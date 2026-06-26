function basicAuthHeader(username, password) {
  return "Basic " + btoa(`${username}:${password}`);
}

async function callApi(path, options = {}) {
  const resp = await fetch(path, options);
  const text = await resp.text();
  let payload = text;
  try {
    payload = text ? JSON.parse(text) : {};
  } catch (_) {
    // keep raw text
  }
  if (!resp.ok) {
    throw new Error(`${resp.status} ${resp.statusText}\n${typeof payload === "string" ? payload : JSON.stringify(payload, null, 2)}`);
  }
  return payload;
}

function printJson(el, data) {
  el.value = typeof data === "string" ? data : JSON.stringify(data, null, 2);
}
