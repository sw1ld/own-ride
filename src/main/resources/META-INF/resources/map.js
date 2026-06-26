const mapElement = document.getElementById('map');
let map = null;

if (mapElement) {
  map = L.map('map').setView([49.4521, 11.0767], 10); // Position: Nuremberg
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors'
  }).addTo(map);
}

let currentPolyline = null;
let startMarker = null;
let endMarker = null;
let hoverMarker = null;
let currentRoute = [];

window.showHoverPointOnMap = function(index) {
  if (map && currentRoute && currentRoute[index]) {
    const p = currentRoute[index];
    const latLng = [p.lat, p.lon];
    if (!hoverMarker) {
      const primaryColor = getComputedStyle(document.documentElement).getPropertyValue('--primary-color').trim() || '#0B4587';
      hoverMarker = L.circleMarker(latLng, {
        radius: 8,
        color: primaryColor,
        fillColor: primaryColor,
        fillOpacity: 0.8
      }).addTo(map);
    } else {
      hoverMarker.setLatLng(latLng);
    }
  }
};

window.hideHoverPointOnMap = function() {
  if (map && hoverMarker) {
    map.removeLayer(hoverMarker);
    hoverMarker = null;
  }
};

window.loadRoute = async function(id) {
  if (!map) return;
  try {
    const response = await fetch(`/fit/activities/id/${encodeURIComponent(id)}`, {
      headers: {
        'Accept': 'application/json'
      },
    });

    if (!response.ok) throw new Error("HTTP " + response.status);

    const data = await response.json();
    currentRoute = data.positions;

    // Remove old route and markers
    if (currentPolyline) map.removeLayer(currentPolyline);
    if (startMarker) map.removeLayer(startMarker);
    if (endMarker) map.removeLayer(endMarker);
    window.hideHoverPointOnMap();

    // Draw new route
    const latLngs = currentRoute.map(p => [p.lat, p.lon]);
    const primaryColor = getComputedStyle(document.documentElement).getPropertyValue('--primary-color').trim() || '#0B4587';
    currentPolyline = L.polyline(latLngs, { color: primaryColor, weight: 4 }).addTo(map);

    map.on('mousemove', function(e) {
      if (!currentRoute || currentRoute.length === 0) return;

      const latlng = e.latlng;
      // Check if mouse is near the route
      // Optimization: only check every 5th point for distance calculation
      let minIdx = -1;
      let minDist = Infinity;
      const step = 5; 
      
      for(let i=0; i<latLngs.length; i += step) {
        const d = latlng.distanceTo(latLngs[i]);
        if (d < minDist) {
          minDist = d;
          minIdx = i;
        }
      }

      if (minIdx === -1) return;

      // Tolerance: only show if mouse is within a reasonable distance (e.g. 100 meters or based on zoom)
      // A more robust way is to check distance in pixels
      const pixelDist = map.latLngToLayerPoint(latlng).distanceTo(map.latLngToLayerPoint(latLngs[minIdx]));

      if (pixelDist < 50) { // 50 pixel tolerance
        window.showHoverPointOnMap(minIdx);
        if (window.highlightAltitudePoint) {
          window.highlightAltitudePoint(minIdx);
        }
      } else {
        window.hideHoverPointOnMap();
        if (window.highlightAltitudePoint) {
          window.highlightAltitudePoint(-1);
        }
      }
    });

    // Add start/end markers
    startMarker = L.marker(latLngs[0]).addTo(map).bindPopup("Start");
    endMarker = L.marker(latLngs[latLngs.length - 1]).addTo(map).bindPopup("End");

    // Zoom map to route
    map.fitBounds(currentPolyline.getBounds());

    updateAltitudeChart(currentRoute, data.distance);

  } catch (err) {
    console.error(err);
    alert("Failed to load route.");
  }
}
