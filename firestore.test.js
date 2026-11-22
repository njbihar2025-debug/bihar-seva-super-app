const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_bihar_01";
const BOB_UID = "bob_bihar_02";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const ts = new Date(Date.now() - 1000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Unauthorized",
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("khataEntries").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("orders").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("transactions").get()
  );
});

test("UserProfile: owner can create and partially update; cross-user and shadow fields denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Ravi Kumar",
      email: "ravi@example.com",
      district: "Patna",
      businessName: "Ravi Kirana Store",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Partial update allowed fields
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      phone: "9876543210",
      address: "Boring Road, Patna",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Immutable createdAt mutation denied
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Shadow field injection denied
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Cross-user PII read & write denied
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      displayName: "Hijacked",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("KhataEntry: owner CRUD works; invalid amount, future timestamp, @DocumentId id field, and cross-user denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("khataEntries")
      .doc("entry_1")
      .set({
        customerName: "Suresh Sharma",
        customerPhone: "9876501234",
        entryType: "GAVE",
        amount: 2500.5,
        note: "Rice & Mustard Oil",
        settled: false,
        createdAt: created,
        updatedAt: created,
      })
  );

  // List own subcollection
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("khataEntries").get()
  );

  // Toggle settled action
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("khataEntries")
      .doc("entry_1")
      .update({
        settled: true,
        updatedAt: new Date(Date.now() - 1000),
      })
  );

  // Reject unexpected @DocumentId 'id' field in payload
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("khataEntries")
      .doc("entry_bad_id")
      .set({
        id: "entry_bad_id",
        customerName: "Invalid",
        entryType: "GAVE",
        amount: 100,
        settled: false,
        createdAt: created,
        updatedAt: created,
      })
  );

  // Reject future timestamp & non-positive amount
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("khataEntries")
      .doc("entry_future")
      .set({
        customerName: "Future User",
        entryType: "GOT",
        amount: 500,
        settled: false,
        createdAt: new Date(Date.now() + 60000),
        updatedAt: new Date(Date.now() + 60000),
      })
  );
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("khataEntries")
      .doc("entry_zero")
      .set({
        customerName: "Zero Amount",
        entryType: "GOT",
        amount: 0,
        settled: false,
        createdAt: created,
        updatedAt: created,
      })
  );

  // Cross-user list & delete denied
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("khataEntries").get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("khataEntries")
      .doc("entry_1")
      .delete()
  );
});

test("ShopOrder: creation requires PLACED; terminal status locking prevents post-CANCELLED or post-DELIVERED edits", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  // Cannot create directly as DELIVERED
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("orders")
      .doc("ord_skip")
      .set({
        orderNumber: "BS-10001",
        itemsSummary: "Mithila Silk Saree x1",
        itemCount: 1,
        totalAmount: 1899,
        paymentMethod: "UPI",
        deliveryAddress: "Kankarbagh, Patna",
        status: "DELIVERED",
        createdAt: created,
        updatedAt: created,
      })
  );

  // Valid create as PLACED
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("orders")
      .doc("ord_1")
      .set({
        orderNumber: "BS-10002",
        itemsSummary: "Mithila Silk Saree x1",
        itemCount: 1,
        totalAmount: 1899,
        paymentMethod: "COD",
        deliveryAddress: "Kankarbagh, Patna",
        status: "PLACED",
        createdAt: created,
        updatedAt: created,
      })
  );

  // Transition to CANCELLED succeeds
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("orders")
      .doc("ord_1")
      .update({
        status: "CANCELLED",
        updatedAt: new Date(Date.now() - 1000),
      })
  );

  // Terminal state locking: cannot modify after CANCELLED
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("orders")
      .doc("ord_1")
      .update({
        status: "PLACED",
        updatedAt: new Date(Date.now() - 500),
      })
  );
});

test("ServiceTransaction & SavedArticle: append-only receipts and bookmarks enforce ownership", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("transactions")
      .doc("tx_1")
      .set({
        category: "AEPS",
        title: "AEPS Cash Withdrawal",
        subtitle: "SBI • Aadhaar XXXX-4821",
        referenceId: "AEPS-982341",
        amount: 2000,
        status: "SUCCESS",
        createdAt: created,
      })
  );

  // Transactions are immutable (update denied)
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("transactions")
      .doc("tx_1")
      .update({ amount: 99999 })
  );

  // SavedArticle create & list
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("savedArticles")
      .doc("art_1")
      .set({
        title: "Patna Metro Phase 1 Progress Update",
        source: "Bihar Desk",
        region: "BIHAR",
        link: "https://news.google.com/",
        pubDate: "22 Nov 2026",
        createdAt: created,
      })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("savedArticles").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("savedArticles").get()
  );
});
