document.addEventListener("DOMContentLoaded", () => {
  const params = new URLSearchParams(window.location.search);
  let urlYear = params.get("year");

  const currentYear = new Date().getFullYear();

  // No year-Param -> redirect with current year
  if (!urlYear && document.getElementById("yearFilter")) {
    urlYear = String(currentYear);
    params.set("year", urlYear);
    const newUrl = `${window.location.pathname}?${params.toString()}`;
    if (window.location.href !== new URL(newUrl, window.location.origin).href) {
        window.location.href = newUrl;
    }
    return;
  }

  const yearFilter = document.getElementById("yearFilter");
  if (yearFilter) {
    // If the year in the URL is not in the options, take the first available or current one
    const options = [...yearFilter.options].map(opt => opt.value);
    if (!options.includes(urlYear)) {
        urlYear = options[0] || String(currentYear);
        params.set("year", urlYear);
        window.location.href = `${window.location.pathname}?${params.toString()}`;
        return;
    }
    yearFilter.value = urlYear;
  }

  const yearLinks = document.querySelectorAll(".year-link");
  function updateNavbarLinks(year) {
    yearLinks.forEach(link => {
      try {
          const url = new URL(link.href, window.location.origin);
          url.searchParams.set("year", year);
          link.href = url.toString();
      } catch (e) {
          console.error("Invalid URL in year-link", link.href);
      }
    });
  }

  if (urlYear) {
    updateNavbarLinks(urlYear);
  }

  if (yearFilter) {
    yearFilter.addEventListener("change", () => {
      const year = yearFilter.value;
      if (!year) return;

      params.set("year", year);
      window.location.href = `${window.location.pathname}?${params.toString()}`;
    });
  }
});
