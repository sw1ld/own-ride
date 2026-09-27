document.addEventListener('DOMContentLoaded', () => {
    const fileInput = document.getElementById('fileInput');
    const fileName = document.getElementById('fileName');
    const selectFilesBtn = document.getElementById('selectFilesBtn');
    const uploadForm = document.getElementById('uploadForm');

    if (!uploadForm) return;

    const updateLabel = (input) => {
        const count = input.files.length;
        if (count === 1) {
            fileName.textContent = input.files[0].name;
        } else if (count > 1) {
            fileName.textContent = count + ' files selected';
        } else {
            fileName.textContent = 'No files selected';
        }
    };

    selectFilesBtn.onclick = () => {
        fileInput.click();
    };

    fileInput.onchange = () => updateLabel(fileInput);

    uploadForm.onsubmit = (e) => {
        if (fileInput.files.length === 0) {
            e.preventDefault();
            alert('Please select at least one file.');
        }

        // Disable empty file inputs to avoid sending empty parts
        if (fileInput.files.length === 0) {
            fileInput.disabled = true;
        }
    };
});
