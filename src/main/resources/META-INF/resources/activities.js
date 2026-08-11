document.addEventListener('DOMContentLoaded', () => {
    let draggedId = null;
    let draggedType = null;
    let currentDragOverCard = null;

    document.addEventListener('click', (e) => {
        const card = e.target.closest('.clickable-card');
        if (!card) return;
        if (e.target.closest('button') || e.target.closest('a') || e.target.closest('.rating')) return;

        const type = card.dataset.type;
        const id = card.dataset.id;
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
