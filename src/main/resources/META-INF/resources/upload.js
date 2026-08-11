document.addEventListener('DOMContentLoaded', () => {
    const fileInput = document.getElementById('fileInput');
    const folderInput = document.getElementById('folderInput');
    const fileName = document.getElementById('fileName');
    const selectFilesBtn = document.getElementById('selectFilesBtn');
    const selectFolderBtn = document.getElementById('selectFolderBtn');
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
        folderInput.value = ''; // Reset folder input
        fileInput.click();
    };

    selectFolderBtn.onclick = () => {
        fileInput.value = ''; // Reset file input
        folderInput.click();
    };

    fileInput.onchange = () => updateLabel(fileInput);
    folderInput.onchange = () => updateLabel(folderInput);

    uploadForm.onsubmit = (e) => {
        if (fileInput.files.length === 0 && folderInput.files.length === 0) {
            e.preventDefault();
            alert('Please select at least one file or folder.');
        }

        // Disable empty file inputs to avoid sending empty parts
        if (fileInput.files.length === 0) {
            fileInput.disabled = true;
        }
        if (folderInput.files.length === 0) {
            folderInput.disabled = true;
        }
    };
});
