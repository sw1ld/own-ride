async function loadStatistics() {
    const urlParams = new URLSearchParams(window.location.search);
    const year = urlParams.get('year') || new Date().getFullYear();

    try {
        const response = await fetch(`/own/stats?year=${year}`, {
            headers: {
                'Accept': 'application/json'
            }
        });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const stats = await response.json();

        // stats.tourDates is an object with ISO date as key and distance as value
        // Since we use a TreeMap in the backend, the keys arrive sorted.
        const dailyDates = Object.keys(stats.tourDates);
        const dailyDistance = Object.values(stats.tourDates);

        initStatistics(dailyDates, dailyDistance);

        // Update Summary if elements exist
        const ridesCount = document.getElementById('ridesCount');
        const totalDistance = document.getElementById('totalDistance');
        if (ridesCount) ridesCount.textContent = stats.rides;
        if (totalDistance) totalDistance.textContent = stats.distance;

    } catch (e) {
        console.error("Error loading statistics data", e);
    }
}

function initStatistics(dailyDates, dailyDistance) {
    const theme = getChartTheme();

    const createBarChart = (canvasId, labels, data, xLabel, yLabel) => {
        const options = getBaseOptions(xLabel, yLabel);
        
        createChart(canvasId, {
            type: 'bar',
            data: {
                labels,
                datasets: [{
                    label: 'Distance',
                    data,
                    backgroundColor: theme.barColor,
                    borderRadius: 4
                }]
            },
            options: options
        });
    };

    createBarChart(
        "routesInYearChart",
        dailyDates,
        dailyDistance,
        '',
        'kilometers'
    );

    const distances = dailyDistance.filter(v => v > 0);
    const binCount = 10;
    const min = 0;
    const max = 100;
    const binSize = (max - min) / binCount;

    const bins = new Array(binCount).fill(0).map((_, i) => ({
        start: min + i * binSize,
        end: min + (i + 1) * binSize,
        count: 0
    }));

    distances.forEach(value => {
        const index = Math.min(
            Math.floor((value - min) / binSize),
            binCount - 1
        );
        if (index >= 0) {
            bins[index].count++;
        }
    });

    const labelPerBin = bins.map(b => Math.round(b.end).toString());
    const dataPerBin = bins.map(b => b.count);

    createBarChart(
        "routeLengthChart",
        labelPerBin,
        dataPerBin,
        'kilometers',
        'active days'
    );
}
