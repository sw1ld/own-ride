let altitudeChart = null;
let currentFilteredRoute = [];
let routeOffset = 0;

function updateAltitudeChart(route, distance) {
  const ctx = document.getElementById('altitudeChart').getContext('2d');

  // Filter leading zero altitudes
  let firstNonZeroIndex = route.findIndex(p => p.altitude && p.altitude > 0);
  if (firstNonZeroIndex === -1) firstNonZeroIndex = 0;
  
  routeOffset = firstNonZeroIndex;
  currentFilteredRoute = route.slice(firstNonZeroIndex);

  const totalDistance = parseFloat(distance) || 0;

  // Smoothing: use last value if current is 0 or missing
  let lastAltitude = 0;
  const altitudes = currentFilteredRoute.map(p => {
    if (p.altitude !== undefined && p.altitude !== null && p.altitude !== 0) {
      lastAltitude = p.altitude;
    }
    return lastAltitude;
  });

  const labels = currentFilteredRoute.map((_, index) => index);

  if (altitudeChart) {
    altitudeChart.destroy();
  }

  // Determine tick interval
  let interval = 1;
  if (totalDistance > 50) interval = 10;
  else if (totalDistance > 10) interval = 5;

  altitudeChart = new Chart(ctx, {
    type: 'line',
    data: {
      labels: labels,
      datasets: [{
        label: 'Altitude (m)',
        data: altitudes,
        borderColor: 'rgb(75, 192, 192)',
        backgroundColor: 'rgba(75, 192, 192, 0.2)',
        fill: true,
        tension: 0.1,
        pointRadius: 0
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        x: {
          display: true,
          title: {
            display: true,
            text: 'Distance (km)'
          },
          ticks: {
            callback: function(value, index) {
              const totalPoints = altitudes.length;
              const dist = (index / (totalPoints - 1)) * totalDistance;
              
              // Only show label if the point is closest to an interval
              // We check if this index is the one that best represents a multiple of the interval
              const targetDistance = Math.round(dist / interval) * interval;
              const targetIndex = Math.round((targetDistance / totalDistance) * (totalPoints - 1));
              
              if (index === targetIndex || index === 0 || index === totalPoints - 1) {
                return Math.round(dist) + " km";
              }
              return null;
            },
            autoSkip: true,
            maxRotation: 0
          }
        }
      },
      plugins: {
        tooltip: {
          enabled: true,
          intersect: false,
          mode: 'index',
          axis: 'x', // Improved tolerance for horizontal movement
          callbacks: {
            title: function(context) {
              const index = context[0].dataIndex;
              const dist = (index / (altitudes.length - 1)) * totalDistance;
              return `Distance: ${dist.toFixed(2)} km`;
            }
          }
        }
      },
      hover: {
        mode: 'index',
        intersect: false,
        axis: 'x'
      },
      onHover: (event, chartElements) => {
        if (chartElements.length > 0) {
          const index = chartElements[0].index;
          // Optimization: Only update map every 5th point when hovering over the chart
          if (index % 5 !== 0 && index !== altitudes.length - 1) return;

          const originalIndex = index + routeOffset;
          if (window.showHoverPointOnMap) {
            window.showHoverPointOnMap(originalIndex);
          }
        } else {
            if (window.hideHoverPointOnMap) {
                window.hideHoverPointOnMap();
            }
        }
      }
    }
  });
}

window.highlightAltitudePoint = function(originalIndex) {
    if (!altitudeChart) return;
    
    const chartIndex = originalIndex - routeOffset;
    if (chartIndex >= 0 && chartIndex < currentFilteredRoute.length) {
        altitudeChart.setActiveElements([{
            datasetIndex: 0,
            index: chartIndex
        }]);
        altitudeChart.tooltip.setActiveElements([{
            datasetIndex: 0,
            index: chartIndex
        }], {
            x: 0,
            y: 0
        });
        altitudeChart.update();
    } else {
        altitudeChart.setActiveElements([]);
        altitudeChart.tooltip.setActiveElements([], {x: 0, y: 0});
        altitudeChart.update();
    }
};
