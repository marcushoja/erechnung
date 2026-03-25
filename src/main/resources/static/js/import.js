(() => {
    const importForm = document.getElementById("import-form");
    const fileInput = document.getElementById("import-file-input");

    if (!importForm || !fileInput) {
        return;
    }

    fileInput.addEventListener("change", () => {
        if (fileInput.files && fileInput.files.length > 0) {
            importForm.submit();
        }
    });
})();
