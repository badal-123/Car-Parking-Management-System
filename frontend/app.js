const DEFAULT_API_BASE = 'http://localhost:8080/api/parking';
const API_BASE = (localStorage.getItem('parkingApiBase') || DEFAULT_API_BASE).replace(/\/$/, '');

const message = document.getElementById('message');
const slotContainer = document.getElementById('slots');

async function safeJson(response) {
  const text = await response.text();
  if (!text) return {};
  try {
    return JSON.parse(text);
  } catch {
    return { message: text };
  }
}

async function fetchDashboard() {
  const response = await fetch(`${API_BASE}/dashboard`);
  const data = await safeJson(response);
  if (!response.ok) throw new Error(data.message || 'Failed to load dashboard');

  document.getElementById('totalSlots').textContent = data.totalSlots;
  document.getElementById('occupiedSlots').textContent = data.occupiedSlots;
  document.getElementById('availableSlots').textContent = data.availableSlots;

  slotContainer.innerHTML = data.slots
    .map(
      (slot) => `<div class="slot ${slot.occupied ? 'occupied' : ''}">
          <strong>Slot ${slot.slotNumber}</strong><br>
          Section ${slot.section}<br>
          ${slot.occupied ? `Vehicle: ${slot.vehicleNumber}` : 'Free'}
        </div>`
    )
    .join('');
}

async function post(path, payload) {
  const response = await fetch(`${API_BASE}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });

  const body = await safeJson(response);
  if (!response.ok) {
    throw new Error(body.message || 'Request failed');
  }
  return body;
}

document.getElementById('checkInForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    const vehicleNumber = document.getElementById('vehicleNumber').value.trim();
    const data = await post('/check-in', { vehicleNumber });
    message.textContent = `Checked in successfully:\n${JSON.stringify(data, null, 2)}`;
    event.target.reset();
    await fetchDashboard();
  } catch (error) {
    message.textContent = error.message;
  }
});

document.getElementById('checkOutForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    const ticketId = document.getElementById('ticketId').value.trim();
    const data = await post('/check-out', { ticketId });
    message.textContent = `Checked out successfully:\n${JSON.stringify(data, null, 2)}`;
    event.target.reset();
    await fetchDashboard();
  } catch (error) {
    message.textContent = error.message;
  }
});

fetchDashboard().catch((error) => {
  message.textContent = `Backend is not reachable at ${API_BASE}.\n${error.message}`;
});
