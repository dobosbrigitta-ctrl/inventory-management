let rawProducts = [];
let displayProducts = [];
let isGrouped = false;
let sortDirections = { name: true, barcode: true };
let activeEditTarget = null;
let rescuePieChartInstance = null;
let criticalBarChartInstance = null;

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
    fetch('/api/products')
        .then(response => response.json())
        .then(products => {
            rawProducts = products;
            processAndRender();
        })
        .catch(err => console.error("Error fetching inventory:", err));
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
            const key = (p.name || "").toLowerCase() + "_" + (p.barcode || "");
            if (!groups[key]) {
                groups[key] = {
                    ...p,
                    id: null,
                    count: 1,
                    originalPrice: p.originalPrice || 0,
                    currentPrice: p.currentPrice || 0,
                    discountPercent: p.discountPercent || 0
                };
            } else {
                groups[key].quantity += (p.quantity || 0);
                groups[key].originalPrice += (p.originalPrice || 0);
                groups[key].currentPrice += (p.currentPrice || 0);
                groups[key].count += 1;
            }
        });
        displayProducts = Object.values(groups).map(g => {
            g.originalPrice = Math.round(g.originalPrice / g.count);
            g.currentPrice = Math.round(g.currentPrice / g.count);
            return g;
        });
    } else {
        displayProducts = rawProducts.map(p => ({
            ...p,
            originalPrice: p.originalPrice || 0,
            currentPrice: p.currentPrice || 0,
            discountPercent: p.discountPercent || 0
        }));
    }
    renderTable(displayProducts);
    updateDashboardCharts();
}

function renderTable(products) {
    const tbody = document.getElementById('inventoryTableBody');
    tbody.innerHTML = '';

    if (!products || products.length === 0) {
        return;
    }

    products.forEach(product => {
        const qty = product.quantity || 0;
        const isCritical = qty <= 5;
        const rowClass = isCritical ? 'class="critical-stock"' : '';
        const paramId = product.id !== null ? product.id : 'null';

        let priceHtml = '';
        const discPercent = product.discountPercent || 0;
        const origPrice = product.originalPrice || 0;
        const currPrice = product.currentPrice || 0;

        if (discPercent > 0) {
            priceHtml = `<span class="old-price">${origPrice} Ft</span> <span class="current-price">${currPrice} Ft</span><span class="discount-badge">-%${discPercent}</span>`;
        } else {
            priceHtml = `<span class="current-price">${origPrice} Ft</span>`;
        }

        const row = `<tr ${rowClass}>
            <td>${product.name || ''}</td>
            <td>${product.barcode || ''}</td>
            <td class="${isGrouped ? 'hide-col' : ''}">${product.expirationDate || ''}</td>
            <td>${qty}</td>
            <td>${priceHtml}</td>
            <td>
                <button class="icon-btn" title="Edit Stock" onclick="openModal(${paramId}, '${product.name || ''}', '${product.barcode || ''}')">✏️</button>
                <button class="icon-btn" title="Delete Product" onclick="deleteProduct(${paramId}, '${product.name || ''}', '${product.barcode || ''}')">🗑️</button>
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
    let javaMode = mode === 'SUB' ? 'reduce' : 'add';
    let url = `/api/products/update-stock?amount=${amount}&mode=${javaMode}`;
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

    let url = `/api/products/delete?`;
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
        let valA = (a[column] || "").toString().toUpperCase();
        let valB = (b[column] || "").toString().toUpperCase();

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
    const originalPrice = document.getElementById('originalPrice').value;

    fetch(`/api/products/add?name=${encodeURIComponent(name)}&barcode=${barcode}&expirationDate=${expirationDate}&quantity=${quantity}&originalPrice=${originalPrice}`)
        .then(() => {
            document.getElementById('productForm').reset();
            fetchInventory();
        });
});

function updateDashboardCharts() {
    const pieCanvas = document.getElementById('rescuePieChart');
    const barCanvas = document.getElementById('criticalBarChart');
    if (!pieCanvas || !barCanvas) return;

    const criticalItems = rawProducts.filter(p => (p.quantity || 0) <= 5);
    const barLabels = criticalItems.map(p => p.name || 'Unknown');
    const barData = criticalItems.map(p => p.quantity || 0);

    if (criticalBarChartInstance) criticalBarChartInstance.destroy();
    criticalBarChartInstance = new Chart(barCanvas, {
        type: 'bar',
        data: {
            labels: barLabels,
            datasets: [{
                label: 'Stock Quantity',
                data: barData,
                backgroundColor: 'rgba(216, 59, 1, 0.2)',
                borderColor: 'rgba(216, 59, 1, 1)',
                borderWidth: 2,
                borderRadius: 6,
                borderSkipped: false,
                hoverBackgroundColor: 'rgba(216, 59, 1, 0.4)'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            animation: {
                duration: 1200,
                easing: 'easeOutQuart'
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { stepSize: 1, color: '#666' },
                    grid: { color: 'rgba(0, 0, 0, 0.05)' }
                },
                x: {
                    ticks: { color: '#666' },
                    grid: { display: false }
                }
            }
        }
    });

    fetch('/api/products/stats')
        .then(response => response.json())
        .then(stats => {
            let standardCount = stats.standard || 0;
            let rescueCount = stats.rescue || 0;

            if (standardCount === 0 && rescueCount === 0) {
                rescueCount = rawProducts.filter(p => (p.discountPercent || 0) > 0).length;
                standardCount = rawProducts.length - rescueCount;
            }

            if (rescuePieChartInstance) rescuePieChartInstance.destroy();
            rescuePieChartInstance = new Chart(pieCanvas, {
                type: 'doughnut',
                data: {
                    labels: ['Standard Items', 'Rescue Items'],
                    datasets: [{
                        data: [standardCount, rescueCount],
                        backgroundColor: ['rgba(78, 115, 223, 0.85)', 'rgba(28, 200, 138, 0.85)'],
                        borderColor: ['#4e73df', '#1cc88a'],
                        borderWidth: 2,
                        hoverOffset: 12
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { position: 'bottom', labels: { boxWidth: 15, padding: 15 } }
                    },
                    animation: {
                        duration: 1500,
                        easing: 'easeOutElastic'
                    },
                    cutout: '65%'
                }
            });
        })
        .catch(err => console.error("Error loading pie chart stats:", err));
}


function switchTab(tabId) {
    const contents = document.querySelectorAll('.tab-content');
    contents.forEach(content => {
        content.style.display = 'none';
    });

    const activeTab = document.getElementById(tabId);
    if (activeTab) {
        activeTab.style.display = 'block';
    }

    const buttons = document.querySelectorAll('.tab-btn');
    buttons.forEach(btn => btn.classList.remove('active'));

    const clickedButton = document.querySelector(`[onclick*="${tabId}"]`);
    if (clickedButton) {
        clickedButton.classList.add('active');
    }
}
function openModal(id, name, barcode) {
    const product = rawProducts.find(p => p.id === id || (p.name === name && p.barcode === barcode));

    activeEditTarget = { id, name, barcode };
    document.getElementById('modalTitle').innerText = "Edit: " + name;
    document.getElementById('modalSubTitle').innerText = "Barcode: " + barcode;

    if (product) {
        document.getElementById('editOriginalPrice').value = product.originalPrice || 0;
        document.getElementById('editDiscountPercent').value = product.discountPercent || 0;
    }

    document.getElementById('editModal').style.display = 'flex';
}
function executePriceUpdate() {
    if (!activeEditTarget) return;

    const newName = document.getElementById('editProductName').value.trim();
    const newPrice = parseInt(document.getElementById('editOriginalPrice').value);
    const newDiscount = parseInt(document.getElementById('editDiscountPercent').value) || 0;

    if (!newName) {
        alert("Please enter a valid product name!");
        return;
    }

    if (isNaN(newPrice) || newPrice < 0) {
        alert("Please enter a valid price!");
        return;
    }

    let url = `/api/products/update-price?name=${encodeURIComponent(newName)}&price=${newPrice}&discount=${newDiscount}`;
    if (activeEditTarget.id !== null) {
        url += `&id=${activeEditTarget.id}`;
    } else {
        url += `&originalName=${encodeURIComponent(activeEditTarget.name)}&barcode=${activeEditTarget.barcode}`;
    }

    fetch(url).then(res => {
        if (res.ok) {
            closeModal();
            fetchInventory();
        } else {
            alert("Failed to update product details! Check your backend endpoint.");
        }
    });
}


