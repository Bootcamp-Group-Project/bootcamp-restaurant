// Shared front-end data store for demo pages.
const CANTEEN_STORE_KEY = "canteen_demo_state_v1";
const CANTEEN_IMAGE_STORE_KEY = "canteen_demo_images_v1";
const CANTEEN_ORDERS_KEY = "canteen_demo_orders_v1";
const CANTEEN_SESSION_KEY = "canteen_session";

const LOGIN_BASE  = "/api/login";
const ORDER_BASE  = "/api/order";
const WALLET_BASE = "/api/wallet";
const PICKUP_BASE = "/api/pickup";

function getSession() {
  try {
    const raw = localStorage.getItem(CANTEEN_SESSION_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch (_) { return null; }
}

function saveSession(data) {
  localStorage.setItem(CANTEEN_SESSION_KEY, JSON.stringify(data));
}

function clearSession() {
  localStorage.removeItem(CANTEEN_SESSION_KEY);
}

function getCurrentUserId() {
  const s = getSession();
  return (s && s.userId) ? s.userId : 1;
}
const CANTEEN_IMAGE_MAX_WIDTH = 800;
const CANTEEN_IMAGE_JPEG_QUALITY = 0.82;

const CANTEEN_DEFAULT_PLACEHOLDER_IMAGE =
  "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=400";

const CANTEEN_DEFAULT_STATE = {
  dishes: [
    {
      id: 1,
      name: "香煎雞扒飯",
      price: 48,
      balance: 12,
      status: "ON_LIST",
      image: "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=400",
      imageFileName: "",
      options: ["Less Rice", "No Onion", "Extra Sauce"]
    },
    {
      id: 2,
      name: "咖哩牛腩飯",
      price: 52,
      balance: 0,
      status: "SOLD_OUT",
      image: "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400",
      imageFileName: "",
      options: ["Less Rice", "Mild Spicy", "No Coriander"]
    },
    {
      id: 3,
      name: "蒜香豬扒飯",
      price: 46,
      balance: 6,
      status: "ON_LIST",
      image: "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400",
      imageFileName: "",
      options: []
    }
  ],
  drinks: [
    {
      id: 101,
      name: "檸檬茶",
      price: 12,
      status: "ON_LIST",
      imageFileName: "",
      options: ["Less Ice", "No Sugar", "Extra Lemon"]
    },
    {
      id: 102,
      name: "奶茶",
      price: 14,
      status: "ON_LIST",
      imageFileName: "",
      options: ["Less Ice", "Less Sugar", "No Ice"]
    },
    {
      id: 103,
      name: "凍咖啡",
      price: 16,
      status: "ON_LIST",
      imageFileName: "",
      options: ["Less Ice", "No Sugar", "Extra Shot"]
    }
  ],
  teacherPublished: { dishes: [], drinks: [] },
  studentPublished: { dishes: [], drinks: [] }
};

function cloneDefaultState() {
  return JSON.parse(JSON.stringify(CANTEEN_DEFAULT_STATE));
}

function deepClone(value) {
  return JSON.parse(JSON.stringify(value));
}

function emptyPublishedMenu() {
  return { dishes: [], drinks: [] };
}

function itemImageKey(type, id) {
  return `${type}-${id}`;
}

function loadImageStore() {
  try {
    const raw = localStorage.getItem(CANTEEN_IMAGE_STORE_KEY);
    if (!raw) return {};
    const parsed = JSON.parse(raw);
    return parsed && typeof parsed === "object" ? parsed : {};
  } catch (_) {
    return {};
  }
}

function saveItemImage(key, dataUrl) {
  const store = loadImageStore();
  store[key] = dataUrl;
  localStorage.setItem(CANTEEN_IMAGE_STORE_KEY, JSON.stringify(store));
  notifyStoreUpdated();
}

function getItemImage(key) {
  if (!key) return null;
  return loadImageStore()[key] || null;
}

function removeItemImage(key) {
  if (!key) return;
  const store = loadImageStore();
  delete store[key];
  localStorage.setItem(CANTEEN_IMAGE_STORE_KEY, JSON.stringify(store));
}

function notifyStoreUpdated() {
  window.dispatchEvent(new CustomEvent("canteen-store-updated"));
}

function compressImageDataUrl(dataUrl, maxWidth, quality) {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.onload = () => {
      const scale = img.width > maxWidth ? maxWidth / img.width : 1;
      const w = Math.max(1, Math.round(img.width * scale));
      const h = Math.max(1, Math.round(img.height * scale));
      const canvas = document.createElement("canvas");
      canvas.width = w;
      canvas.height = h;
      const ctx = canvas.getContext("2d");
      ctx.drawImage(img, 0, 0, w, h);
      resolve(canvas.toDataURL("image/jpeg", quality));
    };
    img.onerror = () => reject(new Error("無法讀取圖片"));
    img.src = dataUrl;
  });
}

function compressImageFile(file, maxWidth, quality) {
  const width = maxWidth || CANTEEN_IMAGE_MAX_WIDTH;
  const q = quality || CANTEEN_IMAGE_JPEG_QUALITY;
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      compressImageDataUrl(e.target.result, width, q).then(resolve).catch(reject);
    };
    reader.onerror = () => reject(new Error("無法讀取檔案"));
    reader.readAsDataURL(file);
  });
}

function hasCustomUploadedImage(item) {
  return Boolean(
    item &&
      (item.imageStorageKey ||
        item.imageFileName ||
        (item.image && String(item.image).startsWith("data:")) ||
        item.imageDataUrl)
  );
}

function stripInlineBinaryImages(items) {
  if (!Array.isArray(items)) return;
  items.forEach(item => {
    if (item.imageDataUrl) delete item.imageDataUrl;
    if (item.image && String(item.image).startsWith("data:")) {
      delete item.image;
    }
    // 已上載自訂圖：只保留 imageStorageKey，避免 refresh 時回退到預設 Unsplash
    if (item.imageStorageKey) {
      delete item.image;
      delete item.imageUrl;
    }
  });
}

/** 從圖片庫還原 imageStorageKey（相容舊資料）。 */
function rehydrateMenuImagesFromStore(state) {
  state.dishes.forEach(d => {
    const key = itemImageKey("dish", d.id);
    if (getItemImage(key)) {
      d.imageStorageKey = key;
    }
  });
  state.drinks.forEach(d => {
    const key = itemImageKey("drink", d.id);
    if (getItemImage(key)) {
      d.imageStorageKey = key;
    }
  });
}

function ensureImageKeysBeforeSave(state) {
  rehydrateMenuImagesFromStore(state);
  state.dishes.forEach(d => {
    if (d.imageStorageKey && !getItemImage(d.imageStorageKey)) {
      const fallbackKey = itemImageKey("dish", d.id);
      if (getItemImage(fallbackKey)) d.imageStorageKey = fallbackKey;
    }
  });
  state.drinks.forEach(d => {
    if (d.imageStorageKey && !getItemImage(d.imageStorageKey)) {
      const fallbackKey = itemImageKey("drink", d.id);
      if (getItemImage(fallbackKey)) d.imageStorageKey = fallbackKey;
    }
  });
}

function stripStateBinaryPayload(state) {
  stripInlineBinaryImages(state.dishes);
  stripInlineBinaryImages(state.drinks);
  if (state.teacherPublished) {
    stripInlineBinaryImages(state.teacherPublished.dishes);
    stripInlineBinaryImages(state.teacherPublished.drinks);
  }
  if (state.studentPublished) {
    stripInlineBinaryImages(state.studentPublished.dishes);
    stripInlineBinaryImages(state.studentPublished.drinks);
  }
}

function ensurePublishedMenus(state) {
  if (!state.teacherPublished || !Array.isArray(state.teacherPublished.dishes)) {
    state.teacherPublished = emptyPublishedMenu();
  }
  if (!state.studentPublished || !Array.isArray(state.studentPublished.dishes)) {
    state.studentPublished = emptyPublishedMenu();
  }
  if (!Array.isArray(state.teacherPublished.drinks)) {
    state.teacherPublished.drinks = [];
  }
  if (!Array.isArray(state.studentPublished.drinks)) {
    state.studentPublished.drinks = [];
  }
}

function refreshDishStatuses(state) {
  state.dishes = state.dishes.map(d => {
    const next = {
      ...d,
      status: Number(d.balance) > 0 ? "ON_LIST" : "SOLD_OUT"
    };
    if (d.imageStorageKey) next.imageStorageKey = d.imageStorageKey;
    if (d.imageFileName) next.imageFileName = d.imageFileName;
    return next;
  });
}

/** Resolve display URL: image store key > inline data > remote URL. */
function resolveItemImage(item) {
  if (!item) return CANTEEN_DEFAULT_PLACEHOLDER_IMAGE;
  const key = item.imageStorageKey || "";
  if (key) {
    const stored = getItemImage(key);
    if (stored) return stored;
  }
  if (item.imageDataUrl) return item.imageDataUrl;
  if (hasCustomUploadedImage(item)) {
    return CANTEEN_DEFAULT_PLACEHOLDER_IMAGE;
  }
  if (item.image && String(item.image).trim()) return item.image;
  if (item.imageUrl && String(item.imageUrl).trim()) return item.imageUrl;
  if (item.photo && String(item.photo).trim()) return item.photo;
  return CANTEEN_DEFAULT_PLACEHOLDER_IMAGE;
}

function cloneDishForFrontend(dish) {
  const status = Number(dish.balance) > 0 ? "ON_LIST" : "SOLD_OUT";
  const image = resolveItemImage(dish);
  return {
    id: dish.id,
    name: dish.name,
    price: Number(dish.price) || 0,
    balance: Number(dish.balance) || 0,
    status,
    options: Array.isArray(dish.options) ? [...dish.options] : [],
    imageStorageKey: dish.imageStorageKey || "",
    imageFileName: dish.imageFileName || "",
    image,
    imageUrl: dish.imageUrl || image
  };
}

function cloneDrinkForFrontend(drink) {
  const image = resolveItemImage(drink);
  return {
    id: drink.id,
    name: drink.name,
    price: Number(drink.price) || 0,
    status: drink.status || "ON_LIST",
    options: Array.isArray(drink.options) ? [...drink.options] : [],
    imageStorageKey: drink.imageStorageKey || "",
    imageFileName: drink.imageFileName || "",
    image,
    imageUrl: drink.imageUrl || image
  };
}

/**
 * Atomic sync: clear Teacher & Student published menus, then insert full Admin snapshot.
 */
function syncToFrontend(state) {
  refreshDishStatuses(state);
  ensurePublishedMenus(state);

  state.teacherPublished = emptyPublishedMenu();
  state.studentPublished = emptyPublishedMenu();

  const dishSnapshots = state.dishes.map(cloneDishForFrontend);
  const drinkSnapshots = state.drinks.map(cloneDrinkForFrontend);

  state.teacherPublished.dishes.push(...dishSnapshots.map(deepClone));
  state.teacherPublished.drinks.push(...drinkSnapshots.map(deepClone));
  state.studentPublished.dishes.push(...dishSnapshots.map(deepClone));
  state.studentPublished.drinks.push(...drinkSnapshots.map(deepClone));

  return state;
}

function loadCanteenState() {
  try {
    const raw = localStorage.getItem(CANTEEN_STORE_KEY);
    if (!raw) {
      const initial = cloneDefaultState();
      refreshDishStatuses(initial);
      syncToFrontend(initial);
      saveCanteenState(initial);
      return initial;
    }
    const parsed = JSON.parse(raw);
    if (!parsed || !Array.isArray(parsed.dishes) || !Array.isArray(parsed.drinks)) {
      const fallback = cloneDefaultState();
      refreshDishStatuses(fallback);
      syncToFrontend(fallback);
      saveCanteenState(fallback);
      return fallback;
    }
    ensurePublishedMenus(parsed);
    rehydrateMenuImagesFromStore(parsed);
    refreshDishStatuses(parsed);
    return parsed;
  } catch (_) {
    const fallback = cloneDefaultState();
    refreshDishStatuses(fallback);
    syncToFrontend(fallback);
    saveCanteenState(fallback);
    return fallback;
  }
}

function saveCanteenState(state) {
  refreshDishStatuses(state);
  ensurePublishedMenus(state);
  ensureImageKeysBeforeSave(state);
  const payload = deepClone(state);
  stripStateBinaryPayload(payload);
  try {
    localStorage.setItem(CANTEEN_STORE_KEY, JSON.stringify(payload));
    notifyStoreUpdated();
  } catch (e) {
    const msg =
      e && e.name === "QuotaExceededError"
        ? "儲存失敗：瀏覽器空間不足，請刪除部分菜品圖片後再試。"
        : "儲存失敗：" + (e.message || e);
    throw new Error(msg);
  }
}

/** Save admin working copy and push to Teacher / Student published menus. */
function saveCanteenStateAndSync(state) {
  syncToFrontend(state);
  saveCanteenState(state);
  return state;
}

/**
 * Upload & persist image for a dish/drink, then sync to Teacher/Student.
 * @returns {Promise<{imageStorageKey: string, imageFileName: string}>}
 */
async function persistMenuItemImage(type, id, file) {
  if (!file) throw new Error("未選擇圖片檔案");
  const compressed = await compressImageFile(file);
  const key = itemImageKey(type, id);
  saveItemImage(key, compressed);
  return {
    imageStorageKey: key,
    imageFileName: file.name,
    previewUrl: compressed
  };
}

/** 套用新上載圖片；未選新檔時請勿呼叫。 */
function applyUploadedImageToItem(item, uploadResult) {
  item.imageStorageKey = uploadResult.imageStorageKey;
  item.imageFileName = uploadResult.imageFileName;
  delete item.imageDataUrl;
  delete item.image;
  delete item.imageUrl;
}

/**
 * @param {"teacher"|"student"} role
 */
function loadFrontendMenu(role) {
  const state = loadCanteenState();
  const key = role === "teacher" ? "teacherPublished" : "studentPublished";
  const published = state[key];
  if (
    published &&
    (published.dishes.length > 0 || published.drinks.length > 0)
  ) {
    return {
      dishes: published.dishes.map(cloneDishForFrontend),
      drinks: published.drinks.map(cloneDrinkForFrontend)
    };
  }
  return {
    dishes: state.dishes.map(cloneDishForFrontend),
    drinks: state.drinks.map(cloneDrinkForFrontend)
  };
}

function deleteDishFromState(state, id) {
  const dish = state.dishes.find(d => d.id === id);
  if (dish && dish.imageStorageKey) {
    removeItemImage(dish.imageStorageKey);
  }
  state.dishes = state.dishes.filter(d => d.id !== id);
  return state;
}

function deleteDrinkFromState(state, id) {
  const drink = state.drinks.find(d => d.id === id);
  if (drink && drink.imageStorageKey) {
    removeItemImage(drink.imageStorageKey);
  }
  state.drinks = state.drinks.filter(d => d.id !== id);
  return state;
}

const CANTEEN_ORDER_WINDOW_KEY = "canteen_demo_order_window_v1";

const CANTEEN_ORDER_WINDOW_DEFAULT = {
  slots: [
    { start: "11:00", end: "12:00" },
    { start: "13:00", end: "14:30" }
  ],
  zone: "Asia/Hong_Kong",
  enforced: true
};

const CANTEEN_ORDER_WINDOW = { ...CANTEEN_ORDER_WINDOW_DEFAULT };

function buildOrderWindowClosedMessage(cfg) {
  const c = cfg || CANTEEN_ORDER_WINDOW_DEFAULT;
  const slotsText = (c.slots || []).map(s => `${s.start} - ${s.end}`).join(" 或 ");
  return `目前不是訂購時段，請在訂購時段：${slotsText} 進行訂餐，謝謝﹗`;
}

function _normalizeOrderWindow(parsed) {
  let slots;
  if (Array.isArray(parsed.slots)) {
    // keep only valid slot objects
    slots = parsed.slots.filter(s => s && s.start && s.end);
  }
  if (!slots || !slots.length) {
    // backward compat: old format had top-level {start, end}
    slots = (parsed.start && parsed.end)
      ? [{ start: parsed.start, end: parsed.end }]
      : CANTEEN_ORDER_WINDOW_DEFAULT.slots;
  }
  return {
    slots,
    zone: parsed.zone || CANTEEN_ORDER_WINDOW_DEFAULT.zone,
    enforced: parsed.enforced !== undefined ? parsed.enforced : CANTEEN_ORDER_WINDOW_DEFAULT.enforced
  };
}

function loadOrderWindow() {
  try {
    const raw = localStorage.getItem(CANTEEN_ORDER_WINDOW_KEY);
    if (!raw) return { ...CANTEEN_ORDER_WINDOW_DEFAULT };
    const parsed = JSON.parse(raw);
    if (!parsed || typeof parsed !== "object") return { ...CANTEEN_ORDER_WINDOW_DEFAULT };
    return _normalizeOrderWindow(parsed);
  } catch (_) {
    return { ...CANTEEN_ORDER_WINDOW_DEFAULT };
  }
}

function saveOrderWindow(cfg) {
  const merged = _normalizeOrderWindow({ ...CANTEEN_ORDER_WINDOW_DEFAULT, ...(cfg || {}) });
  localStorage.setItem(CANTEEN_ORDER_WINDOW_KEY, JSON.stringify(merged));
  notifyStoreUpdated();
  return merged;
}

function parseTimeToMinutes(timeText) {
  const [h, m] = String(timeText).split(":").map(Number);
  return h * 60 + m;
}

function getNowMinutesInOrderZone(zone) {
  const parts = new Intl.DateTimeFormat("en-GB", {
    timeZone: zone || CANTEEN_ORDER_WINDOW_DEFAULT.zone,
    hour: "2-digit",
    minute: "2-digit",
    hour12: false
  }).formatToParts(new Date());
  const hour = Number(parts.find(p => p.type === "hour").value);
  const minute = Number(parts.find(p => p.type === "minute").value);
  return hour * 60 + minute;
}

function isOrderWindowOpen() {
  const cfg = loadOrderWindow();
  const now = getNowMinutesInOrderZone(cfg.zone);
  return (cfg.slots || []).some(slot => {
    const start = parseTimeToMinutes(slot.start);
    const end   = parseTimeToMinutes(slot.end);
    return now >= start && now <= end;
  });
}

function assertOrderWindowOpen() {
  const cfg = loadOrderWindow();
  if (!cfg.enforced || isOrderWindowOpen()) return true;
  alert(buildOrderWindowClosedMessage(cfg));
  return false;
}

/** 統計購物車中每款 Dish 數量 */
function countDishQuantitiesInCart(cartItems) {
  const qtyById = {};
  cartItems
    .filter(item => item.type === "dish")
    .forEach(item => {
      qtyById[item.id] = (qtyById[item.id] || 0) + 1;
    });
  return qtyById;
}

/**
 * 送出訂單前檢查 Dish 庫存（Drink 不扣庫存）。
 * @returns {{ ok: boolean, message?: string }}
 */
function validateOrderStock(state, cartItems) {
  const qtyById = countDishQuantitiesInCart(cartItems);
  for (const idStr of Object.keys(qtyById)) {
    const id = Number(idStr);
    const need = qtyById[id];
    const dish = state.dishes.find(d => d.id === id);
    if (!dish) {
      return { ok: false, message: `菜品 #${id} 不存在或已下架。` };
    }
    const balance = Number(dish.balance) || 0;
    if (balance <= 0 || dish.status === "SOLD_OUT") {
      return { ok: false, message: `「${dish.name}」已售完，請調整購物車後再送出。` };
    }
    if (need > balance) {
      return {
        ok: false,
        message: `「${dish.name}」庫存不足：剩餘 ${balance} 份，購物車需要 ${need} 份。`
      };
    }
  }
  return { ok: true };
}

/**
 * 訂單完成後扣減 Dish balance，並同步 Admin / Student / Teacher 菜單。
 */
function deductStockForOrder(state, cartItems) {
  const qtyById = countDishQuantitiesInCart(cartItems);
  Object.keys(qtyById).forEach(idStr => {
    const dish = state.dishes.find(d => d.id === Number(idStr));
    if (!dish) return;
    dish.balance = Math.max(0, (Number(dish.balance) || 0) - qtyById[idStr]);
  });
  refreshDishStatuses(state);
  syncToFrontend(state);
  saveCanteenState(state);
  return state;
}

function loadOrders() {
  try {
    const raw = localStorage.getItem(CANTEEN_ORDERS_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed : [];
  } catch (_) {
    return [];
  }
}

function saveOrders(orders) {
  localStorage.setItem(CANTEEN_ORDERS_KEY, JSON.stringify(orders));
  notifyStoreUpdated();
}

function appendOrder(order) {
  const orders = loadOrders();
  orders.push({
    ...order,
    status: order.status || "PENDING"
  });
  saveOrders(orders);
}

function formatDateInOrderZone(date) {
  return new Intl.DateTimeFormat("en-CA", {
    timeZone: loadOrderWindow().zone
  }).format(date);
}

function isOrderToday(order) {
  if (!order || !order.createdAt) return false;
  const created = new Date(order.createdAt);
  return formatDateInOrderZone(created) === formatDateInOrderZone(new Date());
}

function getTodayOrders() {
  return loadOrders()
    .filter(isOrderToday)
    .sort((a, b) => b.id - a.id);
}

function getTodayOrdersForRole(role) {
  return getTodayOrders().filter(o => o.role === role);
}

function completeOrder(orderId) {
  const orders = loadOrders();
  const order = orders.find(o => o.id === orderId);
  if (!order) return null;
  order.status = "READY";
  order.completedAt = new Date().toISOString();
  order.statusMessage = "廚房已完成您的訂單，請取餐。謝謝﹗";
  saveOrders(orders);
  return order;
}

function formatOrderItemsSummary(items) {
  if (!items || !items.length) return "（無項目）";
  const counts = {};
  items.forEach(item => {
    const label = item.type === "drink" ? `${item.name}（飲）` : item.name;
    counts[label] = (counts[label] || 0) + 1;
  });
  return Object.entries(counts)
    .map(([name, qty]) => `${name} x${qty}`)
    .join(", ");
}

function buildTodaySalesReport(orders, menuState) {
  const today = orders || [];
  const completed = today.filter(o => ["COMPLETED","READY","PICKED_UP"].includes(o.status));
  const revenue = today.reduce((s, o) => s + (Number(o.total) || 0), 0);
  const dishCounts = {};
  today.forEach(o => {
    (o.items || [])
      .filter(i => i.type === "dish")
      .forEach(i => {
        dishCounts[i.name] = (dishCounts[i.name] || 0) + 1;
      });
  });
  let topDish = "—";
  let topCount = 0;
  Object.entries(dishCounts).forEach(([name, count]) => {
    if (count > topCount) {
      topCount = count;
      topDish = name;
    }
  });
  const soldOutCount = (menuState?.dishes || []).filter(
    d => d.status === "SOLD_OUT" || Number(d.balance) <= 0
  ).length;
  return {
    totalOrders: today.length,
    completedOrders: completed.length,
    pendingOrders: today.length - completed.length,
    revenue,
    topDish: topCount > 0 ? `${topDish}（${topCount}）` : "—",
    soldOutCount
  };
}

/**
 * 依 Dish 統計銷量（數量與金額），並回傳總計。
 * 金額採用訂單成立時記錄的單價（item.price），不受其後改價影響。
 * 僅統計 Dish（Drink 不列入）。
 * @returns {{ rows: Array<{id:number,name:string,qty:number,amount:number}>, totalQty:number, totalAmount:number }}
 */
function buildDishSalesReport(orders) {
  const list = orders || [];
  const byDish = {};
  list.forEach(o => {
    (o.items || [])
      .filter(i => i.type === "dish")
      .forEach(i => {
        const key = i.id != null ? `id-${i.id}` : `name-${i.name}`;
        if (!byDish[key]) {
          byDish[key] = { id: i.id, name: i.name, qty: 0, amount: 0 };
        }
        byDish[key].qty += 1;
        byDish[key].amount += Number(i.price) || 0;
      });
  });
  const rows = Object.values(byDish).sort((a, b) => b.qty - a.qty);
  const totalQty = rows.reduce((s, r) => s + r.qty, 0);
  const totalAmount = rows.reduce((s, r) => s + r.amount, 0);
  return { rows, totalQty, totalAmount };
}

/**
 * 依「日期 + 菜式」統計 Dish 銷量，回傳多個日期區塊（新到舊）。
 * 每個區塊含：該日各菜式的售出份數、銷量金額，及當日總計。
 * 金額採用下單當時記錄的單價（item.price）。
 * @returns {Array<{ date:string, rows:Array<{id,name,qty,amount}>, totalQty:number, totalAmount:number }>}
 */
function buildDishSalesReportByDate(orders) {
  const list = orders || [];
  const byDate = {};
  list.forEach(o => {
    if (!o || !o.createdAt) return;
    const date = formatDateInOrderZone(new Date(o.createdAt));
    if (!byDate[date]) byDate[date] = {};
    const bucket = byDate[date];
    (o.items || [])
      .filter(i => i.type === "dish")
      .forEach(i => {
        const key = i.id != null ? `id-${i.id}` : `name-${i.name}`;
        if (!bucket[key]) {
          bucket[key] = { id: i.id, name: i.name, qty: 0, amount: 0 };
        }
        bucket[key].qty += 1;
        bucket[key].amount += Number(i.price) || 0;
      });
  });
  return Object.keys(byDate)
    .sort((a, b) => b.localeCompare(a))
    .map(date => {
      const rows = Object.values(byDate[date]).sort((a, b) => b.qty - a.qty);
      const totalQty = rows.reduce((s, r) => s + r.qty, 0);
      const totalAmount = rows.reduce((s, r) => s + r.amount, 0);
      return { date, rows, totalQty, totalAmount };
    })
    .filter(block => block.rows.length > 0);
}
