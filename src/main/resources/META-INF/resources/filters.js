document.addEventListener("DOMContentLoaded", () => {
  const params = new URLSearchParams(window.location.search);
  let urlYear = params.get("year");

  const currentYear = new Date().getFullYear();

  // Kein year-Param -> redirect mit aktuellem Jahr
  if (!urlYear) {
    urlYear = String(currentYear);
    params.set("year", urlYear);
    window.location.href = `${window.location.pathname}?${params.toString()}`;
    return;
  }

  const yearFilter = document.getElementById("yearFilter");
  if (yearFilter && [...yearFilter.options].some(
      opt => opt.value === urlYear)) {
    yearFilter.value = urlYear;
  }

  const yearLinks = document.querySelectorAll(".year-link");
  function updateNavbarLinks(year) {
    yearLinks.forEach(link => {
      const url = new URL(link.href, window.location.origin);
      url.searchParams.set("year", year);
      link.href = url.toString();
    });
  }

  updateNavbarLinks(urlYear);

  if (yearFilter) {
    yearFilter.addEventListener("change", () => {
      const year = yearFilter.value;
      if (!year) return;

      params.set("year", year);
      window.location.href = `${window.location.pathname}?${params.toString()}`;
    });
  }
});
