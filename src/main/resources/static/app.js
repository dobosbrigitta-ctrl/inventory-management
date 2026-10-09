let rawProducts = [];
let displayProducts = [];
let isGrouped = false;
let sortDirections = { name: true, barcode: true };
let activeEditTarget = null;

document.addEventListener("DOMContentLoaded", fetchInventory);

document.getElementById('expirationDate').addEventListener('input', function (e) {
    let input = e.target.value.replace(/\D/g, '');
    if (input.length > 4 && input.length <= 6) {
        input = input.slice(0, 4) + '-' + input.slice(4);
    } else if (input.length > 6) {
        input = input.slice(0, 4) + '-' + input.slice(4, 6) + '-' + input.slice(6, 8);
    }
    e.target.value = input;
});

function fetchInventory() {
    fetch('/products')
        .then(response => response.json())
        .then(products => {
            rawProducts = products;
            processAndRender();
        });
}

function toggleView() {
    isGrouped = !isGrouped;
    const btn = document.getElementById('viewToggleBtn');
    const thExpiration = document.getElementById('th-expiration');

    if (isGrouped) {
        btn.innerText = "Group by Name: ON";
        btn.classList.add('active');
        thExpiration.classList.add('hide-col');
    } else {
        btn.innerText = "Group by Name: OFF";
        btn.classList.remove('active');
        thExpiration.classList.remove('hide-col');
    }
    processAndRender();
}

function processAndRender() {
    if (isGrouped) {
        const groups = {};
        rawProducts.forEach(p => {
            const key = p.name.toLowerCase() + "_" + p.barcode;
            if (!groups[key]) {
                groups[key] = { ...p, id: null };
            } else {
                groups[key].quantity += p.quantity;
            }
        });
        displayProducts = Object.values(groups);
    } else {
        displayProducts = JSON.parse(JSON.stringify(rawProducts));
    }
    renderTable(displayProducts);
}

function renderTable(products) {
    const tbody = document.getElementById('inventoryTableBody');
    tbody.innerHTML = '';
    products.forEach(product => {
        const isCritical = product.quantity <= 5;
        const rowClass = isCritical ? 'class="critical-stock"' : '';
        const paramId = product.id !== null ? product.id : 'null';

        const row = `<tr ${rowClass}>
            <td>${product.name}</td>
            <td>${product.barcode}</td>
            <td class="${isGrouped ? 'hide-col' : ''}">${product.expirationDate || ''}</td>
            <td>${product.quantity}</td>
            <td>
                <button class="icon-btn" title="Edit Stock" onclick="openModal(${paramId}, '${product.name}', '${product.barcode}')">✏️</button>
                <button class="icon-btn" title="Delete Product" onclick="deleteProduct(${paramId}, '${product.name}', '${product.barcode}')">🗑️</button>
            </td>
        </tr>`;
        tbody.innerHTML += row;
    });
}

function openModal(id, name, barcode) {
    activeEditTarget = { id, name, barcode };
    document.getElementById('modalTitle').innerText = "Edit Stock: " + name;
    document.getElementById('modalSubTitle').innerText = "Barcode: " + barcode;
    document.getElementById('editModal').style.display = 'flex';
}

// Ensure proper closing syntax is strictly retained
function closeModal() {
    document.getElementById('editModal').style.display = 'none';
    activeEditTarget = null;
}

function executeQuickUpdate(amount, mode) {
    sendUpdateReq(amount, mode);
}

function executeCustomUpdate(mode) {
    const amount = parseInt(document.getElementById('customAmount').value);
    if (isNaN(amount) || amount <= 0) {
        alert("Please enter a valid positive number!");
        return;
    }
    sendUpdateReq(amount, mode);
}

function sendUpdateReq(amount, mode) {
    if (!activeEditTarget) return;
    let url = `/products/update-stock?amount=${amount}&mode=${mode}`;
    if (activeEditTarget.id !== null) {
        url += `&id=${activeEditTarget.id}`;
    } else {
        url += `&name=${encodeURIComponent(activeEditTarget.name)}&barcode=${activeEditTarget.barcode}`;
    }

    fetch(url).then(res => {
        if (res.ok) {
            closeModal();
            fetchInventory();
        } else {
            alert("Operation failed! Check your stock levels.");
        }
    });
}

function deleteProduct(id, name, barcode) {
    if (!confirm(`Are you sure you want to delete ${name}?`)) return;

    let url = `/products/delete?`;
    if (id !== null) {
        url += `id=${id}`;
    } else {
        url += `name=${encodeURIComponent(name)}&barcode=${barcode}`;
    }

    fetch(url).then(res => {
        if (res.ok) fetchInventory();
    });
}

function sortInventory(column) {
    sortDirections[column] = !sortDirections[column];

    document.getElementById('name-sort').innerText = '↕';
    document.getElementById('barcode-sort').innerText = '↕';
    document.getElementById(`${column}-sort`).innerText = sortDirections[column] ? '▲' : '▼';

    displayProducts.sort((a, b) => {
        let valA = a[column].toUpperCase();
        let valB = b[column].toUpperCase();

        if (valA < valB) return sortDirections[column] ? -1 : 1;
        if (valA > valB) return sortDirections[column] ? 1 : -1;
        return 0;
    });

    renderTable(displayProducts);
}

document.getElementById('productForm').addEventListener('submit', function(e) {
    e.preventDefault();
    const name = document.getElementById('name').value;
    const barcode = document.getElementById('barcode').value;
    const expirationDate = document.getElementById('expirationDate').value;
    const quantity = document.getElementById('quantity').value;

    fetch(`/products/add?name=${encodeURIComponent(name)}&barcode=${barcode}&expirationDate=${expirationDate}&quantity=${quantity}`)
        .then(() => {
            document.getElementById('productForm').reset();
            fetchInventory();
        });
});
