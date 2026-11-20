"use strict";

const DEFAULT_API = "http://localhost:8080";
const LS = {
  api: "logitrack_api",
  token: "logitrack_token",
  user: "logitrack_user",
  rol: "logitrack_rol"
};

const state = {
  api: DEFAULT_API,
  token: "",
  user: "",
  rol: "",
  productos: [],
  ventas: []
};

const $ = (id) => document.getElementById(id);
const moneyFmt = new Intl.NumberFormat("es-CO", { maximumFractionDigits: 2 });
const money = (v) => "$" + moneyFmt.format(Number.isFinite(Number(v)) ? Number(v) : 0);

function esc(val) {
  return String(val ?? "").replace(/[&<>"']/g, (c) => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
  }[c]));
}

function toast(msg, type = "info") {
  const box = $("toasts");
  const el = document.createElement("div");
  el.className = `toast ${type}`;
  el.textContent = msg;
  box.appendChild(el);
  setTimeout(() => el.remove(), 3500);
}

function logHttp(title, payload) {
  const box = $("httpConsole");
  if (!box) return;
  const line = `[${new Date().toLocaleTimeString()}] ${title}` +
    (payload !== undefined ? `\n${JSON.stringify(payload, null, 2)}` : "");
  box.textContent = `${line}\n\n${box.textContent}`.slice(0, 25000);
}

function cleanToken(raw) {
  return String(raw || "").replace(/^Bearer\s+/i, "").trim();
}

function isTokenExpired(token) {
  try {
    const parts = cleanToken(token).split(".");
    if (parts.length !== 3) return false;
    const base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const json = JSON.parse(atob(base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), "=")));
    return typeof json.exp === "number" && json.exp <= Math.floor(Date.now() / 1000);
  } catch {
    return false;
  }
}

function toArray(data) {
  if (Array.isArray(data)) return data;
  if (data && Array.isArray(data.content)) return data.content;
  if (data && Array.isArray(data.data)) return data.data;
  return [];
}

/* ---------- Cliente HTTP ---------- */
async function api(method, path, { body, auth = true } = {}) {
  const url = `${state.api}${path}`;
  const headers = { Accept: "application/json" };

  if (body !== undefined) {
    headers["Content-Type"] = "application/json";
  }

  if (auth) {
    const tk = cleanToken(state.token);
    if (!tk || isTokenExpired(tk)) {
      logout(true);
      throw new Error("Tu sesión venció. Inicia sesión de nuevo.");
    }
    headers.Authorization = `Bearer ${tk}`;
  }

  logHttp(`${method} ${url}`, body);

  let res;
  try {
    res = await fetch(url, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined
    });
  } catch {
    logHttp("ERROR DE RED / CORS");
    throw new Error("No se pudo conectar con Spring Boot. Revisa que esté encendido y con CORS habilitado.");
  }

  const text = await res.text();
  let data = null;
  if (text) {
    try { data = JSON.parse(text); } catch { data = text; }
  }

  logHttp(`← ${res.status} ${res.statusText}`, data);

  if (!res.ok) {
    if (res.status === 401) logout(true);
    const msg = (data && typeof data === "object" && (data.message || data.error)) ||
      (typeof data === "string" && data) ||
      `Error HTTP ${res.status}`;
    throw new Error(msg);
  }

  return data;
}

/* ---------- Sesión y Vistas ---------- */
function saveSession(authResp, fallbackUser = "") {
  const token = cleanToken(
    typeof authResp === "string"
      ? authResp
      : authResp?.token || authResp?.jwt || authResp?.accessToken || ""
  );
  if (!token) throw new Error("El backend no devolvió ningún token JWT.");

  state.token = token;
  state.user = authResp?.username || fallbackUser;
  state.rol = authResp?.rol || "";

  localStorage.setItem(LS.token, state.token);
  localStorage.setItem(LS.user, state.user);
  localStorage.setItem(LS.rol, state.rol);

  renderSession();
}

function logout(expired = false) {
  state.token = "";
  state.user = "";
  state.rol = "";
  localStorage.removeItem(LS.token);
  localStorage.removeItem(LS.user);
  localStorage.removeItem(LS.rol);
  renderSession();
  if (expired) toast("Sesión expirada, vuelve a ingresar.", "warning");
}

function renderSession() {
  const logged = Boolean(state.token && !isTokenExpired(state.token));
  $("authView").classList.toggle("hidden", logged);
  $("appView").classList.toggle("hidden", !logged);

  if (logged) {
    $("userBadge").textContent = state.rol ? `${state.user} (${state.rol})` : state.user;
    $("navApiLabel").textContent = state.api.replace(/^https?:\/\//, "");
    $("jwtBox").value = state.token;
  }
}

/* ---------- Inicialización ---------- */
document.addEventListener("DOMContentLoaded", () => {
  state.api = (localStorage.getItem(LS.api) || DEFAULT_API).replace(/\/+$/, "");
  state.token = cleanToken(localStorage.getItem(LS.token) || "");
  state.user = localStorage.getItem(LS.user) || "";
  state.rol = localStorage.getItem(LS.rol) || "";

  if (state.token && isTokenExpired(state.token)) {
    logout(false);
  }

  $("apiUrlInput").value = state.api;
  $("swaggerLink").href = `${state.api}/swagger-ui.html`;

  bindEvents();
  renderSession();

  if (state.token) {
    cargarTodo();
  }
});

function bindEvents() {
  // Guardar URL de API
  $("btnSaveApi").addEventListener("click", () => {
    const val = $("apiUrlInput").value.trim().replace(/\/+$/, "");
    if (!/^https?:\/\/.+/.test(val)) {
      toast("Ingresa una URL válida, ej: http://localhost:8080", "warning");
      return;
    }
    state.api = val;
    localStorage.setItem(LS.api, val);
    $("swaggerLink").href = `${val}/swagger-ui.html`;
    toast("URL de API guardada", "success");
  });

  // Tabs Login / Registro
  document.querySelectorAll(".auth-tab").forEach((tab) => {
    tab.addEventListener("click", () => {
      document.querySelectorAll(".auth-tab").forEach((t) => t.classList.remove("active"));
      document.querySelectorAll(".auth-form").forEach((f) => f.classList.remove("active"));
      tab.classList.add("active");
      $(tab.dataset.authTab === "login" ? "loginForm" : "registerForm").classList.add("active");
    });
  });

  // Login -> POST /auth/login
  $("loginForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const username = $("loginUsername").value.trim();
    const password = $("loginPassword").value;
    if (!username || !password) {
      toast("Completa usuario y contraseña", "warning");
      return;
    }
    const btn = $("btnLogin");
    btn.disabled = true;
    btn.textContent = "Entrando...";
    try {
      const resp = await api("POST", "/auth/login", { body: { username, password }, auth: false });
      saveSession(resp, username);
      toast("Bienvenido a LogiTrack", "success");
      await cargarTodo();
    } catch (err) {
      toast(err.message, "error");
    } finally {
      btn.disabled = false;
      btn.textContent = "Entrar";
    }
  });

  // Registro -> POST /auth/register
  $("registerForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const payload = {
      nombreCompleto: $("regNombre").value.trim(),
      username: $("regUsername").value.trim(),
      email: $("regEmail").value.trim(),
      password: $("regPassword").value,
      rol: $("regRol").value
    };
    if (!payload.nombreCompleto || !payload.username || !payload.email || !payload.password) {
      toast("Completa todos los campos de registro", "warning");
      return;
    }
    const btn = $("btnRegister");
    btn.disabled = true;
    btn.textContent = "Creando cuenta...";
    try {
      const resp = await api("POST", "/auth/register", { body: payload, auth: false });
      saveSession(resp, payload.username);
      toast("Cuenta creada correctamente", "success");
      await cargarTodo();
    } catch (err) {
      toast(err.message, "error");
    } finally {
      btn.disabled = false;
      btn.textContent = "Crear cuenta y entrar";
    }
  });

  // Logout
  $("btnLogout").addEventListener("click", () => {
    logout(false);
    toast("Sesión cerrada", "info");
  });

  // Navegación interna
  document.querySelectorAll(".nav-btn").forEach((btn) => {
    btn.addEventListener("click", () => {
      document.querySelectorAll(".nav-btn").forEach((b) => b.classList.remove("active"));
      document.querySelectorAll(".page").forEach((p) => p.classList.remove("active"));
      btn.classList.add("active");
      $(btn.dataset.page).classList.add("active");
    });
  });

  // Productos
  $("btnReloadProductos").addEventListener("click", cargarProductos);
  $("searchProducto").addEventListener("input", pintarProductos);
  $("btnCancelProducto").addEventListener("click", resetProductoForm);
  $("productoForm").addEventListener("submit", guardarProducto);

  $("productosBody").addEventListener("click", (e) => {
    const btn = e.target.closest("button[data-act]");
    if (!btn) return;
    const id = btn.dataset.id;
    if (btn.dataset.act === "edit") editarProducto(id);
    if (btn.dataset.act === "del") eliminarProducto(id);
  });

  // Ventas
  $("btnReloadVentas").addEventListener("click", cargarVentas);
  $("ventaForm").addEventListener("submit", guardarVenta);

  // Token y logs
  $("btnCopyJwt").addEventListener("click", async () => {
    await navigator.clipboard.writeText(state.token);
    toast("Token copiado", "success");
  });
  $("btnClearLog").addEventListener("click", () => {
    $("httpConsole").textContent = "// Consola limpia.";
  });
}

async function cargarTodo() {
  await Promise.all([cargarProductos(), cargarVentas()]);
}

/* ---------- CRUD Productos ---------- */
async function cargarProductos() {
  $("productosBody").innerHTML = `<tr><td colspan="7" class="empty">Cargando...</td></tr>`;
  try {
    state.productos = toArray(await api("GET", "/api/producto"));
    pintarProductos();
  } catch (err) {
    $("productosBody").innerHTML = `<tr><td colspan="7" class="empty">${esc(err.message)}</td></tr>`;
  }
}

function pintarProductos() {
  const q = $("searchProducto").value.trim().toLowerCase();
  const list = state.productos.filter((p) =>
    !q || `${p.nombre ?? ""} ${p.marca ?? ""}`.toLowerCase().includes(q)
  );

  if (!list.length) {
    $("productosBody").innerHTML = `<tr><td colspan="7" class="empty">No hay productos para mostrar.</td></tr>`;
  } else {
    $("productosBody").innerHTML = list.map((p) => {
      const stock = Number(p.stock) || 0;
      const cls = stock === 0 ? "stock-zero" : stock <= 5 ? "stock-low" : "";
      return `
        <tr>
          <td>${esc(p.id)}</td>
          <td><strong>${esc(p.nombre)}</strong></td>
          <td>${esc(p.marca)}</td>
          <td>${money(p.precioCompra)}</td>
          <td>${money(p.precioVenta)}</td>
          <td class="${cls}">${stock}</td>
          <td class="right">
            <button type="button" class="btn btn-secondary btn-sm" data-act="edit" data-id="${esc(p.id)}">Editar</button>
            <button type="button" class="btn btn-danger btn-sm" data-act="del" data-id="${esc(p.id)}">Borrar</button>
          </td>
        </tr>`;
    }).join("");
  }

  const totalUnits = state.productos.reduce((acc, p) => acc + (Number(p.stock) || 0), 0);
  const totalValue = state.productos.reduce((acc, p) => acc + (Number(p.stock) || 0) * (Number(p.precioVenta) || 0), 0);
  $("kpiProdCount").textContent = state.productos.length;
  $("kpiProdStock").textContent = moneyFmt.format(totalUnits);
  $("kpiProdValor").textContent = money(totalValue);
}

async function guardarProducto(e) {
  e.preventDefault();
  const id = $("prodId").value;
  const payload = {
    nombre: $("prodNombre").value.trim(),
    marca: $("prodMarca").value.trim(),
    stock: Number($("prodStock").value),
    precioCompra: Number($("prodCompra").value),
    precioVenta: Number($("prodVenta").value)
  };

  if (!payload.nombre || !payload.marca || payload.stock < 0 || payload.precioCompra < 0 || payload.precioVenta < 0) {
    toast("Revisa los datos del producto", "warning");
    return;
  }

  const btn = $("btnSaveProducto");
  btn.disabled = true;
  try {
    if (id) {
      await api("PUT", `/api/producto/${encodeURIComponent(id)}`, { body: payload });
      toast("Producto actualizado", "success");
    } else {
      await api("POST", "/api/producto", { body: payload });
      toast("Producto creado", "success");
    }
    resetProductoForm();
    await cargarProductos();
  } catch (err) {
    toast(err.message, "error");
  } finally {
    btn.disabled = false;
  }
}

function editarProducto(id) {
  const p = state.productos.find((x) => String(x.id) === String(id));
  if (!p) return;
  $("prodId").value = p.id;
  $("prodNombre").value = p.nombre ?? "";
  $("prodMarca").value = p.marca ?? "";
  $("prodStock").value = p.stock ?? 0;
  $("prodCompra").value = p.precioCompra ?? 0;
  $("prodVenta").value = p.precioVenta ?? 0;
  $("formProductoTitulo").textContent = `Editando Producto #${p.id}`;
}

function resetProductoForm() {
  $("productoForm").reset();
  $("prodId").value = "";
  $("formProductoTitulo").textContent = "Nuevo Producto";
}

async function eliminarProducto(id) {
  if (!confirm(`¿Seguro que deseas eliminar el producto #${id}?`)) return;
  try {
    await api("DELETE", `/api/producto/${encodeURIComponent(id)}`);
    toast("Producto eliminado", "success");
    if ($("prodId").value === String(id)) resetProductoForm();
    await cargarProductos();
  } catch (err) {
    toast(err.message, "error");
  }
}

/* ---------- CRUD Ventas ---------- */
async function cargarVentas() {
  $("ventasBody").innerHTML = `<tr><td colspan="4" class="empty">Cargando...</td></tr>`;
  try {
    state.ventas = toArray(await api("GET", "/api/venta"));
    pintarVentas();
  } catch (err) {
    $("ventasBody").innerHTML = `<tr><td colspan="4" class="empty">${esc(err.message)}</td></tr>`;
  }
}

function pintarVentas() {
  const sorted = [...state.ventas].sort((a, b) => (Number(b.id) || 0) - (Number(a.id) || 0));
  if (!sorted.length) {
    $("ventasBody").innerHTML = `<tr><td colspan="4" class="empty">No hay ventas registradas.</td></tr>`;
  } else {
    $("ventasBody").innerHTML = sorted.map((v) => `
      <tr>
        <td>${esc(v.id)}</td>
        <td>${esc(v.documentoCliente)}</td>
        <td><strong>${esc(v.nombreCompleto)}</strong></td>
        <td class="right">${money(v.total)}</td>
      </tr>
    `).join("");
  }

  const count = state.ventas.length;
  const total = state.ventas.reduce((acc, v) => acc + (Number(v.total) || 0), 0);
  $("kpiVentasCount").textContent = count;
  $("kpiVentasTotal").textContent = money(total);
  $("kpiVentasProm").textContent = money(count ? total / count : 0);
}

async function guardarVenta(e) {
  e.preventDefault();
  const payload = {
    documentoCliente: Number($("ventaDoc").value),
    nombreCompleto: $("ventaCliente").value.trim(),
    total: Number($("ventaTotal").value)
  };

  if (!payload.documentoCliente || !payload.nombreCompleto || payload.total <= 0) {
    toast("Completa correctamente los datos de la venta", "warning");
    return;
  }

  const btn = $("btnSaveVenta");
  btn.disabled = true;
  try {
    await api("POST", "/api/venta", { body: payload });
    toast("Venta registrada", "success");
    $("ventaForm").reset();
    await cargarVentas();
  } catch (err) {
    toast(err.message, "error");
  } finally {
    btn.disabled = false;
  }
}