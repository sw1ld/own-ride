const map = L.map('map').setView([49.4521, 11.0767], 10); // Position: Nuremberg
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  attribution: '&copy; OpenStreetMap contributors'
}).addTo(map);

let currentPolyline = null;
let startMarker = null;
let endMarker = null;

async function loadRoute(filename) {
  try {
    const response = await fetch(`/fit/data/name/${encodeURIComponent(filename)}`);
    if (!response.ok) throw new Error("HTTP " + response.status);

    const data = await response.json();
    const route = data.positions;

    // Remove old route and markers
    if (currentPolyline) map.removeLayer(currentPolyline);
    if (startMarker) map.removeLayer(startMarker);
    if (endMarker) map.removeLayer(endMarker);

    // Draw new route
    currentPolyline = L.polyline(route, { color: 'red', weight: 4 }).addTo(map);

    // Add start/end markers
    startMarker = L.marker(route[0]).addTo(map).bindPopup("Start");
    endMarker = L.marker(route[route.length - 1]).addTo(map).bindPopup("End");

    // Zoom map to route
    map.fitBounds(currentPolyline.getBounds());

  } catch (err) {
    console.error(err);
    alert("Failed to load route.");
  }
}

document.querySelectorAll('.clickable-row').forEach(row => {
  row.addEventListener('click', () => {
    const filename = row.dataset.filename; // assuming each <tr> has data-filename
    loadRoute(filename);
  });
});
