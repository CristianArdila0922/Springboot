/* =====================================================
   LogiTrack - Logica del frontend
   ===================================================== */

const API = "http://localhost:8080";
const TOKEN_KEY = "logitrack_token";
const USER_KEY = "logitrack_user";

let cachedBodegas = [];
let cachedProductos = [];

/* ---------- Utilidades ---------- */
function getToken() { return localStorage.getItem(TOKEN_KEY); }
function setSession(token, user) {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
}
function getStoredUser() {
    try { return JSON.parse(localStorage.getItem(USER_KEY)); } catch (e) { return null; }
}
function clearSession() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
}

async function request(method, path, body) {
    const headers = { "Content-Type": "application/json" };
    const token = getToken();
    if (token) headers["Authorization"] = "Bearer " + token;
    const res = await fetch(API + path, { method, headers, body: body ? JSON.stringify(body) : undefined });
    if (res.status === 204) return null;
    let data = null;
    try { data = await res.json(); } catch (e) { /* sin cuerpo */ }
    if (!res.ok) {
        const msg = (data && (data.mensaje || data.error)) || ("Error " + res.status);
        if (res.status === 401) { clearSession(); showLoginView(); }
        throw new Error(msg);
    }
    return data;
}
const api = {
    get: p => request("GET", p),
    post: (p, b) => request("POST", p, b),
    put: (p, b) => request("PUT", p, b),
    del: p => request("DELETE", p)
};

function toast(msg, type = "") {
    const t = document.createElement("div");
    t.className = "toast " + type;
    t.textContent = msg;
    document.body.appendChild(t);
    setTimeout(() => t.remove(), 3500);
}

function esc(s) {
    return String(s ?? "").replace(/[&<>"']/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}

/* ---------- Vista login / app ---------- */
function showLoginView() {
    document.getElementById("login-view").hidden = false;
    document.getElementById("app-view").hidden = true;
    document.getElementById("user-bar").hidden = true;
}
function showAppView() {
    document.getElementById("login-view").hidden = true;
    document.getElementById("app-view").hidden = false;
    document.getElementById("user-bar").hidden = false;
    const user = getStoredUser();
    document.getElementById("user-info").textContent = "👤 " + (user ? user.username + " (" + user.rol + ")" : "");
    switchTab("bodegas");
}

function showLogin() {
    document.getElementById("login-form").hidden = false;
    document.getElementById("register-form").hidden = true;
    document.getElementById("tab-login").classList.add("active");
    document.getElementById("tab-register").classList.remove("active");
}
function showRegister() {
    document.getElementById("login-form").hidden = true;
    document.getElementById("register-form").hidden = false;
    document.getElementById("tab-login").classList.remove("active");
    document.getElementById("tab-register").classList.add("active");
}

async function doLogin(e) {
    e.preventDefault();
    const msg = document.getElementById("auth-msg");
    msg.textContent = ""; msg.className = "msg";
    try {
        const data = await api.post("/auth/login", {
            username: document.getElementById("login-username").value,
            password: document.getElementById("login-password").value
        });
        setSession(data.token, { username: data.username, rol: data.rol, id: data.userId });
        toast("Bienvenido " + data.username, "success");
        showAppView();
    } catch (err) {
        msg.textContent = err.message; msg.className = "msg error";
    }
    return false;
}

async function doRegister(e) {
    e.preventDefault();
    const msg = document.getElementById("auth-msg");
    msg.textContent = ""; msg.className = "msg";
    try {
        const data = await api.post("/auth/register", {
            username: document.getElementById("reg-username").value,
            password: document.getElementById("reg-password").value,
            email: document.getElementById("reg-email").value || null,
            nombreCompleto: document.getElementById("reg-nombre").value || null,
            rol: document.getElementById("reg-rol").value
        });
        setSession(data.token, { username: data.username, rol: data.rol, id: data.userId });
        toast("Cuenta creada", "success");
        showAppView();
    } catch (err) {
        msg.textContent = err.message; msg.className = "msg error";
    }
    return false;
}

document.getElementById("btn-logout").addEventListener("click", () => {
    clearSession();
    showLoginView();
    toast("Sesion cerrada");
});

/* ---------- Navegacion por pestañas ---------- */
function switchTab(tab) {
    document.querySelectorAll(".nav-btn").forEach(b => b.classList.toggle("active", b.dataset.tab === tab));
    const content = document.getElementById("content");
    content.innerHTML = '<p class="msg">Cargando...</p>';
    const fn = {
        bodegas: renderBodegas,
        productos: renderProductos,
        movimientos: renderMovimientos,
        stockbajo: renderStockBajo,
        auditoria: renderAuditoria,
        reportes: renderReportes
    }[tab];
    fn().catch(err => { content.innerHTML = '<p class="msg error">Error: ' + esc(err.message) + "</p>"; });
}

/* ---------- BODEGAS ---------- */
async function renderBodegas() {
    const bodegas = await api.get("/bodegas");
    cachedBodegas = bodegas;
    const rows = bodegas.map(b => `<tr>
        <td>${b.id}</td><td>${esc(b.nombre)}</td><td>${esc(b.ubicacion)}</td>
        <td>${b.capacidad}</td><td>${esc(b.encargado || "-")}</td>
        <td><button class="btn btn-danger" onclick="deleteBodega(${b.id})">Eliminar</button></td>
    </tr>`).join("");
    document.getElementById("content").innerHTML = `
        <div class="panel"><h3>Nueva bodega</h3>
            <form onsubmit="return createBodega(event)" class="row">
                <label>Nombre<input id="b-nombre" required></label>
                <label>Ubicacion<input id="b-ubi" required></label>
                <label>Capacidad<input id="b-cap" type="number" min="1" required></label>
                <label>Encargado<input id="b-enc"></label>
                <button class="btn btn-primary">Crear</button>
            </form>
        </div>
        <div class="panel"><h3>Bodegas registradas (${bodegas.length})</h3>
            <table><thead><tr><th>ID</th><th>Nombre</th><th>Ubicacion</th><th>Capacidad</th><th>Encargado</th><th></th></tr></thead>
            <tbody>${rows || '<tr><td colspan="6">Sin datos</td></tr>'}</tbody></table>
        </div>`;
}
async function createBodega(e) {
    e.preventDefault();
    try {
        await api.post("/bodegas", {
            nombre: b_nombre.value, ubicacion: b_ubi.value,
            capacidad: +b_cap.value, encargado: b_enc.value || null
        });
        toast("Bodega creada", "success"); renderBodegas();
    } catch (err) { toast(err.message, "error"); }
    return false;
}
async function deleteBodega(id) {
    if (!confirm("¿Eliminar la bodega " + id + "?")) return;
    try { await api.del("/bodegas/" + id); toast("Bodega eliminada", "success"); renderBodegas(); }
    catch (err) { toast(err.message, "error"); }
}

/* ---------- PRODUCTOS ---------- */
async function renderProductos() {
    const productos = await api.get("/productos");
    cachedProductos = productos;
    const rows = productos.map(p => {
        const low = p.stock < 10;
        return `<tr>
            <td>${p.id}</td><td>${esc(p.nombre)}</td><td>${esc(p.categoria)}</td>
            <td><span class="badge ${low ? 'badge-low' : 'badge-ok'}">${p.stock}</span></td>
            <td>$${p.precio}</td>
            <td><button class="btn btn-danger" onclick="deleteProducto(${p.id})">Eliminar</button></td>
        </tr>`;
    }).join("");
    document.getElementById("content").innerHTML = `
        <div class="panel"><h3>Nuevo producto</h3>
            <form onsubmit="return createProducto(event)" class="row">
                <label>Nombre<input id="p-nombre" required></label>
                <label>Categoria<input id="p-cat" required></label>
                <label>Stock<input id="p-stock" type="number" min="0" value="0" required></label>
                <label>Precio<input id="p-precio" type="number" step="0.01" min="0" value="0" required></label>
                <button class="btn btn-primary">Crear</button>
            </form>
        </div>
        <div class="panel"><h3>Productos (${productos.length})</h3>
            <table><thead><tr><th>ID</th><th>Nombre</th><th>Categoria</th><th>Stock</th><th>Precio</th><th></th></tr></thead>
            <tbody>${rows || '<tr><td colspan="6">Sin datos</td></tr>'}</tbody></table>
        </div>`;
}
async function createProducto(e) {
    e.preventDefault();
    try {
        await api.post("/productos", {
            nombre: p_nombre.value, categoria: p_cat.value,
            stock: +p_stock.value, precio: +p_precio.value
        });
        toast("Producto creado", "success"); renderProductos();
    } catch (err) { toast(err.message, "error"); }
    return false;
}
async function deleteProducto(id) {
    if (!confirm("¿Eliminar el producto " + id + "?")) return;
    try { await api.del("/productos/" + id); toast("Producto eliminado", "success"); renderProductos(); }
    catch (err) { toast(err.message, "error"); }
}

/* ---------- MOVIMIENTOS ---------- */
function detalleLine() {
    const opts = cachedProductos.map(p => `<option value="${p.id}">${esc(p.nombre)} (stock ${p.stock})</option>`).join("");
    return `<div class="row" style="align-items:center;margin-bottom:8px">
        <label>Producto<select class="det-prod">${opts}</select></label>
        <label>Cantidad<input class="det-cant" type="number" min="1" value="1"></label>
        <button type="button" class="btn btn-danger" onclick="this.parentElement.remove()">Quitar</button>
    </div>`;
}
async function renderMovimientos() {
    if (!cachedBodegas.length) cachedBodegas = await api.get("/bodegas");
    if (!cachedProductos.length) cachedProductos = await api.get("/productos");
    const movs = await api.get("/movimientos");
    const rows = movs.map(m => `<tr>
        <td>${m.id}</td>
        <td><span class="badge ${m.tipoMovimiento === 'ENTRADA' ? 'badge-entrada' : m.tipoMovimiento === 'SALIDA' ? 'badge-salida' : 'badge-transfer'}">${m.tipoMovimiento}</span></td>
        <td>${(m.fecha || "").replace("T", " ").substring(0, 19)}</td>
        <td>${esc(m.usuario ? m.usuario.username : "-")}</td>
        <td>${esc(m.bodegaOrigen ? m.bodegaOrigen.nombre : "-")}</td>
        <td>${esc(m.bodegaDestino ? m.bodegaDestino.nombre : "-")}</td>
        <td>${(m.detalles || []).map(d => esc(d.producto.nombre) + " x" + d.cantidad).join(", ")}</td>
    </tr>`).join("");
    const bodOpts = cachedBodegas.map(b => `<option value="${b.id}">${esc(b.nombre)}</option>`).join("");
    document.getElementById("content").innerHTML = `
        <div class="panel"><h3>Registrar movimiento</h3>
            <form onsubmit="return createMovimiento(event)">
                <div class="row">
                    <label>Tipo<select id="m-tipo" onchange="toggleBodegas()">
                        <option value="ENTRADA">ENTRADA</option>
                        <option value="SALIDA">SALIDA</option>
                        <option value="TRANSFERENCIA">TRANSFERENCIA</option>
                    </select></label>
                    <label>Bodega origen<select id="m-origen"><option value="">(ninguna)</option>${bodOpts}</select></label>
                    <label>Bodega destino<select id="m-destino"><option value="">(ninguna)</option>${bodOpts}</select></label>
                </div>
                <h4 style="margin:14px 0 6px">Detalles</h4>
                <div id="detalles">${detalleLine()}</div>
                <button type="button" class="btn btn-ghost" style="background:#e0e7ff;color:#1d4ed8;margin-top:6px" onclick="document.getElementById('detalles').insertAdjacentHTML('beforeend', detalleLine())">+ Agregar linea</button>
                <div style="margin-top:12px"><button class="btn btn-success">Registrar movimiento</button></div>
            </form>
        </div>
        <div class="panel"><h3>Movimientos (${movs.length})</h3>
            <table><thead><tr><th>ID</th><th>Tipo</th><th>Fecha</th><th>Usuario</th><th>Origen</th><th>Destino</th><th>Detalles</th></tr></thead>
            <tbody>${rows || '<tr><td colspan="7">Sin movimientos</td></tr>'}</tbody></table>
        </div>`;
    toggleBodegas();
}
function toggleBodegas() {
    const tipo = document.getElementById("m-tipo").value;
    const origen = document.getElementById("m-origen");
    const destino = document.getElementById("m-destino");
    origen.required = (tipo === "SALIDA" || tipo === "TRANSFERENCIA");
    destino.required = (tipo === "ENTRADA" || tipo === "TRANSFERENCIA");
}
async function createMovimiento(e) {
    e.preventDefault();
    try {
        const tipo = document.getElementById("m-tipo").value;
        const origenVal = document.getElementById("m-origen").value;
        const destinoVal = document.getElementById("m-destino").value;
        const detalles = [];
        document.querySelectorAll("#detalles .row").forEach(line => {
            const prodId = line.querySelector(".det-prod").value;
            const cant = +line.querySelector(".det-cant").value;
            if (prodId && cant > 0) detalles.push({ productoId: +prodId, cantidad: cant });
        });
        if (!detalles.length) { toast("Agregue al menos un detalle", "error"); return false; }
        const body = {
            tipoMovimiento: tipo,
            bodegaOrigenId: origenVal ? +origenVal : null,
            bodegaDestinoId: destinoVal ? +destinoVal : null,
            detalles
        };
        await api.post("/movimientos", body);
        toast("Movimiento registrado", "success");
        renderMovimientos();
    } catch (err) { toast(err.message, "error"); }
    return false;
}

/* ---------- STOCK BAJO ---------- */
async function renderStockBajo() {
    const productos = await api.get("/productos/stock-bajo?min=10");
    const rows = productos.map(p => `<tr>
        <td>${p.id}</td><td>${esc(p.nombre)}</td><td>${esc(p.categoria)}</td>
        <td><span class="badge badge-low">${p.stock}</span></td><td>$${p.precio}</td>
    </tr>`).join("");
    document.getElementById("content").innerHTML = `
        <div class="panel"><h3>Productos con stock bajo (&lt; 10 unidades)</h3>
            <table><thead><tr><th>ID</th><th>Nombre</th><th>Categoria</th><th>Stock</th><th>Precio</th></tr></thead>
            <tbody>${rows || '<tr><td colspan="5">No hay productos con stock bajo</td></tr>'}</tbody></table>
        </div>`;
}

/* ---------- AUDITORIA ---------- */
async function renderAuditoria() {
    const data = await api.get("/auditorias");
    const rows = (Array.isArray(data) ? data : []).map(a => `<tr>
        <td>${a.id}</td>
        <td><span class="badge ${a.tipoOperacion === 'DELETE' ? 'badge-salida' : a.tipoOperacion === 'INSERT' ? 'badge-entrada' : 'badge-transfer'}">${a.tipoOperacion}</span></td>
        <td>${(a.fechaHora || "").replace("T", " ").substring(0, 19)}</td>
        <td>${esc(a.usuario)}</td><td>${esc(a.entidad)}</td><td>${esc(a.entidadId || "-")}</td>
        <td><details><summary>Ver valores</summary>
            <div style="font-size:0.78rem;margin-top:4px"><b>Anterior:</b> <code>${esc(a.valorAnterior || "null")}</code><br><b>Nuevo:</b> <code>${esc(a.valorNuevo || "null")}</code></div></details></td>
    </tr>`).join("");
    document.getElementById("content").innerHTML = `
        <div class="panel"><h3>Filtrar auditoria</h3>
            <div class="row">
                <label>Usuario<input id="a-user" placeholder="admin"></label>
                <label>Tipo<select id="a-tipo"><option value="">Todos</option>
                    <option value="INSERT">INSERT</option><option value="UPDATE">UPDATE</option><option value="DELETE">DELETE</option></select></label>
                <button class="btn btn-primary" onclick="filtrarAuditoria()">Consultar</button>
            </div>
        </div>
        <div class="panel"><h3>Registros de auditoria (${(Array.isArray(data) ? data : []).length})</h3>
            <table><thead><tr><th>ID</th><th>Op.</th><th>Fecha</th><th>Usuario</th><th>Entidad</th><th>ID Entidad</th><th>Valores</th></tr></thead>
            <tbody>${rows || '<tr><td colspan="7">Sin registros</td></tr>'}</tbody></table>
        </div>`;
}
async function filtrarAuditoria() {
    const user = document.getElementById("a-user").value.trim();
    const tipo = document.getElementById("a-tipo").value;
    let qs = [];
    if (user) qs.push("usuario=" + encodeURIComponent(user));
    if (tipo) qs.push("tipo=" + tipo);
    try {
        const data = await api.get("/auditorias" + (qs.length ? "?" + qs.join("&") : ""));
        const rows = (Array.isArray(data) ? data : []).map(a => `<tr>
            <td>${a.id}</td>
            <td><span class="badge ${a.tipoOperacion === 'DELETE' ? 'badge-salida' : a.tipoOperacion === 'INSERT' ? 'badge-entrada' : 'badge-transfer'}">${a.tipoOperacion}</span></td>
            <td>${(a.fechaHora || "").replace("T", " ").substring(0, 19)}</td>
            <td>${esc(a.usuario)}</td><td>${esc(a.entidad)}</td><td>${esc(a.entidadId || "-")}</td>
            <td><details><summary>Ver valores</summary>
                <div style="font-size:0.78rem;margin-top:4px"><b>Anterior:</b> <code>${esc(a.valorAnterior || "null")}</code><br><b>Nuevo:</b> <code>${esc(a.valorNuevo || "null")}</code></div></details></td>
        </tr>`).join("");
        document.querySelectorAll(".panel")[1].querySelector("tbody").innerHTML = rows || '<tr><td colspan="7">Sin registros</td></tr>';
    } catch (err) { toast(err.message, "error"); }
}

/* ---------- REPORTES ---------- */
async function renderReportes() {
    const data = await api.get("/reportes/general");
    const stockRows = (data.stockPorBodega || []).map(s => `<tr>
        <td>${s.bodegaId}</td><td>${esc(s.bodegaNombre)}</td><td>${s.total}</td></tr>`).join("");
    const movRows = (data.productosMasMovidos || []).map(p => `<tr>
        <td>${p.productoId}</td><td>${esc(p.productoNombre)}</td><td>${p.total}</td></tr>`).join("");
    document.getElementById("content").innerHTML = `
        <div class="panel"><h3>Stock total por bodega</h3>
            <table><thead><tr><th>ID Bodega</th><th>Nombre</th><th>Total unidades</th></tr></thead>
            <tbody>${stockRows || '<tr><td colspan="3">Sin datos</td></tr>'}</tbody></table>
        </div>
        <div class="panel"><h3>Productos mas movidos</h3>
            <table><thead><tr><th>ID Producto</th><th>Nombre</th><th>Total unidades movidas</th></tr></thead>
            <tbody>${movRows || '<tr><td colspan="3">Sin movimientos</td></tr>'}</tbody></table>
        </div>
        <p class="hint">Generado: ${(data.generadoEn || "").replace("T", " ").substring(0, 19)}</p>`;
}

/* ---------- Inicializacion ---------- */
document.getElementById("api-link").href = API + "/swagger-ui.html";
if (getToken()) showAppView(); else showLoginView();
