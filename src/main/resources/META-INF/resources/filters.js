document.addEventListener("DOMContentLoaded", () => {
  const params = new URLSearchParams(window.location.search);
  let filterUnit = params.get("filterUnit");
  let offset = params.get("offset");

  const filterTabs = {
    WEEK: document.getElementById("weekFilter"),
    MONTH: document.getElementById("monthFilter"),
    YEAR: document.getElementById("yearFilter"),
    ALL: document.getElementById("noFilter")
  };
  const hasFilterTabs = Object.values(filterTabs).some(tab => tab != null);

  // No filterUnit-Param -> redirect with current year
  if (!filterUnit && hasFilterTabs) {
    filterUnit = "YEAR";
    params.set("filterUnit", filterUnit);
    if (offset != null) {
      params.set("offset", offset);
    }
    const newUrl = `${window.location.pathname}?${params.toString()}`;
    if (window.location.href !== new URL(newUrl, window.location.origin).href) {
      window.location.href = newUrl;
    }
    return;
  }

  function navigate(newFilterUnit, newOffset) {
    const newParams = new URLSearchParams(window.location.search);
    newParams.set("filterUnit", newFilterUnit);
    newParams.set("offset", String(newOffset));
    window.location.href = `${window.location.pathname}?${newParams.toString()}`;
  }

  function setActiveTab(activeFilterUnit) {
    Object.entries(filterTabs).forEach(([unit, tab]) => {
      if (!tab) return;
      const li = tab.closest("li");
      if (!li) return;
      li.classList.toggle("is-active", unit === activeFilterUnit);
    });
  }

  if (hasFilterTabs) {
    setActiveTab(filterUnit);

    Object.entries(filterTabs).forEach(([unit, tab]) => {
      if (!tab) return;
      tab.addEventListener("click", () => {
        navigate(unit, 0);
      });
    });
  }

  const currentOffset = offset != null ? parseInt(offset, 10) : 0;
  const paginationPrevious = document.querySelector(".pagination-previous");
  const paginationNext = document.querySelector(".pagination-next");

  if (paginationPrevious) {
    paginationPrevious.addEventListener("click", (event) => {
      event.preventDefault();
      if (paginationPrevious.hasAttribute("disabled")) return;
      navigate(filterUnit, currentOffset + 1);
    });
  }

  if (paginationNext) {
    paginationNext.addEventListener("click", (event) => {
      event.preventDefault();
      if (paginationNext.hasAttribute("disabled")) return;
      navigate(filterUnit, currentOffset - 1);
    });
  }

  const yearLinks = document.querySelectorAll(".filter-unit");
  function updateNavbarLinks(unit) {
    yearLinks.forEach(link => {
      try {
          const url = new URL(link.href, window.location.origin);
          url.searchParams.set("filterUnit", unit);
          link.href = url.toString();
      } catch (e) {
          console.error("Invalid URL in filter-unit", link.href);
      }
    });
  }

  if (filterUnit) {
    updateNavbarLinks(filterUnit);
  }
});
