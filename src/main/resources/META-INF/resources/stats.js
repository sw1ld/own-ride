async function loadStatistics() {
    const urlParams = new URLSearchParams(window.location.search);
    const filterUnit = urlParams.get('filterUnit') || 'MONTH';
    const offset = urlParams.get('offset') || '0';

    try {
        const response = await fetch(`/own/stats?filterUnit=${filterUnit}&offset=${offset}`, {
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

        initStatistics(dailyDates, dailyDistance, filterUnit);

        // Update Summary if elements exist
        const ridesCount = document.getElementById('ridesCount');
        const totalDistance = document.getElementById('totalDistance');
        if (ridesCount) ridesCount.textContent = stats.rides;
        if (totalDistance) totalDistance.textContent = stats.distance;

        updatePaginationState(filterUnit, stats);

    } catch (e) {
        console.error("Error loading statistics data", e);
    }
}

function updatePaginationState(filterUnit, stats) {
    const periodLabel = document.getElementById('periodLabel');
    if (periodLabel) {
        periodLabel.textContent = formatPeriodLabel(filterUnit, stats.start, stats.end);
    }

    const paginationPrevious = document.querySelector('.pagination-previous');
    const paginationNext = document.querySelector('.pagination-next');

    if (paginationPrevious) {
        paginationPrevious.toggleAttribute('disabled', !stats.hasPrevious);
    }
    if (paginationNext) {
        paginationNext.toggleAttribute('disabled', !stats.hasNext);
    }
}

function formatPeriodLabel(filterUnit, isoStart, isoEnd) {
    const start = new Date(isoStart);
    const end = new Date(isoEnd);

    switch (filterUnit) {
        case 'WEEK':
            return `${start.toLocaleDateString()} - ${end.toLocaleDateString()}`;
        case 'MONTH':
            return start.toLocaleDateString(undefined, {month: 'long', year: 'numeric'});
        case 'YEAR':
            return start.getFullYear().toString();
        case 'ALL':
            return 'All';
        default:
            return '';
    }
}

function aggregateByMonth(dailyDates, dailyDistance) {
    const sums = new Map();

    dailyDates.forEach((isoDate, index) => {
        const monthKey = isoDate.substring(0, 7); // "YYYY-MM"
        const value = dailyDistance[index];
        sums.set(monthKey, (sums.get(monthKey) || 0) + value);
    });

    const monthKeys = Array.from(sums.keys()).sort();
    const labels = monthKeys.map(monthKey => {
        const [year, month] = monthKey.split('-');
        const date = new Date(Number(year), Number(month) - 1, 1);
        return date.toLocaleDateString(undefined, {month: 'short', year: 'numeric'});
    });
    const values = monthKeys.map(monthKey => sums.get(monthKey));

    return {labels, values};
}

function initStatistics(dailyDates, dailyDistance, filterUnit) {
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

    const useMonthlyAggregation = filterUnit === 'ALL';
    const {labels: routesLabels, values: routesValues} = useMonthlyAggregation
        ? aggregateByMonth(dailyDates, dailyDistance)
        : {labels: dailyDates, values: dailyDistance};

    createBarChart(
        "routesInYearChart",
        routesLabels,
        routesValues,
        '',
        'kilometers'
    );
}
