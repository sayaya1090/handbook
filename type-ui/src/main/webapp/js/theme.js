let theme = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
document.documentElement.setAttribute('color-theme', theme);
window.matchMedia("(prefers-color-scheme: dark)").addEventListener("change", event => {
    theme = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    document.documentElement.setAttribute('color-theme', theme);
})