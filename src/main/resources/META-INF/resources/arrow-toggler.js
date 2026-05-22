document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('th.sortable').forEach((header, colIndex) => {
    header.addEventListener('click', () => {
      const table = header.closest('table');
      const tbody = table.querySelector('tbody');
      const rows = Array.from(tbody.querySelectorAll('tr'));

      let ascending;
      if (!header.sortState || header.sortState === 'none') {
        ascending = true;
        header.sortState = 'asc';
      } else if (header.sortState === 'asc') {
        ascending = false;
        header.sortState = 'desc';
      } else {
        ascending = null; // neutral
        header.sortState = 'none';
      }

      table.querySelectorAll('th.sortable').forEach(h => {
        h.sortState = h === header ? header.sortState : 'none';
        const arrowSpan = h.querySelector('.arrow');
        if (arrowSpan) {
            arrowSpan.textContent = '↑↓'; // default neutral symbol
        }
      });

      const arrow = header.querySelector('.arrow');
      if (arrow) {
          if (ascending === true) arrow.textContent = '↓';
          else if (ascending === false) arrow.textContent = '↑';
      }

      // If neutral, optionally skip sorting or restore original order (not implemented)
      if (ascending === null) return;

      rows.sort((a, b) => {
        const cellAElement = a.children[colIndex];
        const cellBElement = b.children[colIndex];

        const ratingA = cellAElement.querySelector('.rating');
        const ratingB = cellBElement.querySelector('.rating');

        let cellA, cellB;

        if (ratingA && ratingB) {
            cellA = ratingA.getAttribute('data-rate') || "0";
            cellB = ratingB.getAttribute('data-rate') || "0";
        } else {
            cellA = cellAElement.textContent.trim();
            cellB = cellBElement.textContent.trim();
        }

        const numA = parseFloat(cellA.replace(/[^0-9,.-]/g, '').replace(',', '.'));
        const numB = parseFloat(cellB.replace(/[^0-9,.-]/g, '').replace(',', '.'));

        // Special handling for dates (YYYY-MM-DD)
        const dateRegex = /^\d{4}-\d{2}-\d{2}$/;
        if (dateRegex.test(cellA) && dateRegex.test(cellB)) {
            return ascending ? cellA.localeCompare(cellB) : cellB.localeCompare(cellA);
        }

        if (!isNaN(numA) && !isNaN(numB) && !dateRegex.test(cellA)) {
          return ascending ? numA - numB : numB - numA;
        }
        return ascending ? cellA.localeCompare(cellB) : cellB.localeCompare(cellA);
      });

      rows.forEach(row => tbody.appendChild(row));
    });
  });
});
