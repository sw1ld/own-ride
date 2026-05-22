let altitudeChart = null;
let currentFilteredRoute = [];
let routeOffset = 0;

function updateAltitudeChart(route, distance) {
  const theme = getChartTheme();
  
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

  const options = getBaseOptions('distance', 'altitude', { beginAtZero: false });
  
  // Customizations for Altitude Profile
  options.plugins.tooltip = {
    enabled: true,
    intersect: false,
    mode: 'index',
    axis: 'x',
    callbacks: {
      title: function(context) {
        const index = context[0].dataIndex;
        const dist = (index / (altitudes.length - 1)) * totalDistance;
        return `Distance: ${dist.toFixed(2)} km`;
      }
    }
  };
  
  options.scales.x.ticks = {
    color: theme.subtitleColor,
    callback: function(value, index) {
      const totalPoints = altitudes.length;
      const dist = (index / (totalPoints - 1)) * totalDistance;
      const targetDistance = Math.round(dist / interval) * interval;
      const targetIndex = Math.round((targetDistance / totalDistance) * (totalPoints - 1));
      
      // Always show 0 and the very last point
      if (index === 0 || index === totalPoints - 1) {
        return Math.round(dist) + " km";
      }

      // Show interval points, but only if they are not too close to the end
      if (index === targetIndex) {
        // Prevent overlap with the last label (totalPoints - 1)
        // If the gap to the end is less than half an interval, don't show the penultimate interval label
        const remainingDist = totalDistance - dist;
        if (remainingDist > interval * 0.5) {
            return Math.round(dist) + " km";
        }
      }
      return null;
    },
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
  };

  altitudeChart = createChart('altitudeChart', {
    type: 'line',
    data: {
      labels: labels,
      datasets: [{
        label: 'Altitude (m)',
        data: altitudes,
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
