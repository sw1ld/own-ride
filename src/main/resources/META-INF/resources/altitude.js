let altitudeChart = null;
let currentFilteredRoute = [];
let routeOffset = 0;

function getDistance(p1, p2) {
  if (!p1 || !p2) return 0;
  const R = 6371; // Radius of the earth in km
  const dLat = (p2.lat - p1.lat) * Math.PI / 180;
  const dLon = (p2.lon - p1.lon) * Math.PI / 180;
  const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(p1.lat * Math.PI / 180) * Math.cos(p2.lat * Math.PI / 180) *
      Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c; // Distance in km
}


function updateAltitudeChart(route, distance) {
  const theme = getChartTheme();
  
  // Filter leading zero altitudes
  let firstNonZeroIndex = route.findIndex(p => p.altitude && p.altitude > 0);
  if (firstNonZeroIndex === -1) firstNonZeroIndex = 0;
  
  routeOffset = firstNonZeroIndex;
  currentFilteredRoute = route.slice(firstNonZeroIndex);

  const totalDistance = parseFloat(distance) || 0;

  // Smoothing: use last value if current is 0 or missing
  // And calculate cumulative distance
  let lastAltitude = 0;
  let cumulativeDistance = 0;
  const chartData = currentFilteredRoute.map((p, index) => {
    if (p.altitude !== undefined && p.altitude !== null && p.altitude !== 0) {
      lastAltitude = p.altitude;
    }
    if (index > 0) {
      cumulativeDistance += getDistance(currentFilteredRoute[index - 1], p);
    }
    return { x: cumulativeDistance, y: lastAltitude };
  });

  if (altitudeChart) {
    altitudeChart.destroy();
  }

  // Determine tick interval
  let interval = 1;
  if (totalDistance > 50) interval = 10;
  else if (totalDistance > 10) interval = 5;

  const options = getBaseOptions('distance', 'altitude', { beginAtZero: false });
  
  // Customizations for Altitude Profile
  options.plugins.tooltip = {
    enabled: true,
    intersect: false,
    mode: 'index',
    axis: 'x',
    callbacks: {
      title: function(context) {
        const dist = context[0].parsed.x;
        return `Distance: ${dist.toFixed(2)} km`;
      }
    }
  };
  
  options.scales.x.type = 'linear';
  options.scales.x.min = 0;
  options.scales.x.max = totalDistance;
  options.scales.x.ticks = {
    color: theme.subtitleColor,
    callback: function(value) {
      return Math.round(value) + " km";
    },
    stepSize: interval,
    autoSkip: true,
    maxRotation: 0
  };
  
  options.hover = {
    mode: 'index',
    intersect: false,
    axis: 'x'
  };
  
  options.onHover = (event, chartElements) => {
    if (chartElements.length > 0) {
      const index = chartElements[0].index;
      // Use index from chartData (corresponds to currentFilteredRoute)
      const originalIndex = index + routeOffset;
      if (window.showHoverPointOnMap) {
        window.showHoverPointOnMap(originalIndex);
      }
    } else {
      if (window.hideHoverPointOnMap) {
        window.hideHoverPointOnMap();
      }
    }
  };

  altitudeChart = createChart('altitudeChart', {
    type: 'line',
    data: {
      datasets: [{
        label: 'Altitude (m)',
        data: chartData,
        borderColor: theme.primaryColor,
        backgroundColor: theme.lineBgColor,
        fill: true,
        tension: 0.1,
        pointRadius: 0
      }]
    },
    options: options
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
