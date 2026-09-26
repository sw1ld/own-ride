function applyPendingUpdates() {
    try {
        const pendingRatings = JSON.parse(sessionStorage.getItem('pending_rating_updates') || '{}');
        Object.entries(pendingRatings).forEach(([key, rate]) => {
            const [type, id] = key.split('_');
            document.querySelectorAll(`.rating[data-id="${id}"][data-type="${type}"]`).forEach(el => {
                el.dataset.rate = rate;
                el.querySelectorAll('i').forEach(s => {
                    const val = parseInt(s.dataset.value, 10);
                    s.classList.toggle('active', val <= rate && rate > 0);
                });
            });
        });
    } catch (e) {
        console.error('Error applying pending ratings', e);
    }

    try {
        const pendingNames = JSON.parse(sessionStorage.getItem('pending_name_updates') || '{}');
        Object.entries(pendingNames).forEach(([key, name]) => {
            const [type, id] = key.split('_');
            document.querySelectorAll(`.clickable-card[data-id="${id}"][data-type="${type}"] .title`).forEach(el => {
                el.innerText = name;
            });
        });
    } catch (e) {
        console.error('Error applying pending names', e);
    }
}

function restoreFeedAndScroll() {
    if (sessionStorage.getItem('feed_needs_reload') === 'true') {
        sessionStorage.removeItem('feed_needs_reload');
        sessionStorage.removeItem('feed_restore_html');
        sessionStorage.removeItem('feed_restore_scroll');
        sessionStorage.removeItem('pending_rating_updates');
        sessionStorage.removeItem('pending_name_updates');
        window.location.reload();
        return;
    }

    const savedHtml = sessionStorage.getItem('feed_restore_html');
    const savedScroll = sessionStorage.getItem('feed_restore_scroll');

    if (savedHtml && savedScroll) {
        const feedContainer = document.getElementById('activityFeed');
        if (feedContainer) {
            feedContainer.innerHTML = savedHtml;
            if (window.htmx) {
                htmx.process(feedContainer);
            }
            window.scrollTo({
                top: parseInt(savedScroll, 10),
                behavior: 'instant'
            });
        }
        sessionStorage.removeItem('feed_restore_html');
        sessionStorage.removeItem('feed_restore_scroll');
    }

    applyPendingUpdates();
    sessionStorage.removeItem('pending_rating_updates');
    sessionStorage.removeItem('pending_name_updates');
}

window.addEventListener('pageshow', () => {
    restoreFeedAndScroll();
});

document.addEventListener('DOMContentLoaded', () => {
    restoreFeedAndScroll();

    let draggedId = null;
    let draggedType = null;
    let currentDragOverCard = null;

    document.addEventListener('click', (e) => {
        const card = e.target.closest('.clickable-card');
        if (!card) return;
        if (e.target.closest('button') || e.target.closest('a') || e.target.closest('.rating')) return;

        const type = card.dataset.type;
        const id = card.dataset.id;

        const feedContainer = document.getElementById('activityFeed');
        if (feedContainer) {
            sessionStorage.setItem('feed_restore_html', feedContainer.innerHTML);
            sessionStorage.setItem('feed_restore_scroll', window.scrollY.toString());
        }

        window.location.href = `/own/${type === 'GROUP' ? 'groups' : 'activities'}/id/${id}`;
    });

    document.addEventListener('dragstart', (e) => {
        const card = e.target.closest('.clickable-card');
        if (!card) return;

        draggedId = card.dataset.id;
        draggedType = card.dataset.type;
        e.dataTransfer.setData('text/plain', draggedId);
        card.style.opacity = '0.5';
    });

    document.addEventListener('dragend', (e) => {
        const card = e.target.closest('.clickable-card');
        if (card) card.style.opacity = '1';
        document.querySelectorAll('.clickable-card').forEach(c => c.classList.remove('drag-over'));
        currentDragOverCard = null;
    });

    document.addEventListener('dragover', (e) => {
        const card = e.target.closest('.clickable-card');
        if (!card) return;
        e.preventDefault(); // nötig, damit drop überhaupt erlaubt ist

        if (draggedId !== card.dataset.id && currentDragOverCard !== card) {
            if (currentDragOverCard) currentDragOverCard.classList.remove('drag-over');
            card.classList.add('drag-over');
            currentDragOverCard = card;
        }
    });

    document.addEventListener('dragleave', (e) => {
        const card = e.target.closest('.clickable-card');
        if (!card) return;
        // nur entfernen, wenn wir die Karte wirklich verlassen (nicht nur zu einem Kind-Element wechseln)
        if (!card.contains(e.relatedTarget)) {
            card.classList.remove('drag-over');
            if (currentDragOverCard === card) currentDragOverCard = null;
        }
    });

    document.addEventListener('drop', async (e) => {
        const card = e.target.closest('.clickable-card');
        if (!card) return;
        e.preventDefault();
        card.classList.remove('drag-over');
        currentDragOverCard = null;

        const targetId = card.dataset.id;
        const targetType = card.dataset.type;

        if (draggedId === targetId) return;

        if (draggedType === 'ACTIVITY' && targetType === 'ACTIVITY') {
            const response = await fetch('/own/groups', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify([draggedId, targetId])
            });
            if (response.ok) location.reload();
        } else if (draggedType === 'ACTIVITY' && targetType === 'GROUP') {
            const response = await fetch(`/own/groups/id/${targetId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
                body: JSON.stringify(draggedId)
            });
            if (response.ok) location.reload();
        }
    });
});
