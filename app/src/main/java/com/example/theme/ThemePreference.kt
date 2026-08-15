package com.example.theme

enum class ThemeMode(val label: String) {
    SYSTEM("System Default"),
    DARK("Dark Theme"),
    LIGHT("Light Theme")
}

object DarkModeJsInjector {
    /**
     * JavaScript/CSS payload injected into WebView to enhance night-time reading and high contrast
     */
    val DARK_MODE_CSS_PAYLOAD = """
        (function() {
            var existingStyle = document.getElementById('nightfall-dark-mode-style');
            if (existingStyle) {
                existingStyle.remove();
            }
            var style = document.createElement('style');
            style.id = 'nightfall-dark-mode-style';
            style.innerHTML = `
                html, body {
                    background-color: #0d131f !important;
                    color: #e2e8f0 !important;
                }
                div, section, article, nav, header, footer, aside {
                    background-color: transparent !important;
                    border-color: #243048 !important;
                }
                h1, h2, h3, h4, h5, h6, p, span, li, a {
                    color: #e2e8f0 !important;
                }
                a {
                    color: #d4af37 !important;
                }
                img, video, canvas {
                    filter: brightness(0.9) contrast(1.05) !important;
                    border-radius: 4px;
                }
                table, th, td {
                    background-color: #131b2c !important;
                    border-color: #243048 !important;
                    color: #e2e8f0 !important;
                }
                input, textarea, select {
                    background-color: #172238 !important;
                    color: #ffffff !important;
                    border: 1px solid #d4af37 !important;
                }
                .card, .product-card, .item {
                    background-color: #141d2e !important;
                    box-shadow: 0 4px 6px rgba(0,0,0,0.3) !important;
                }
            `;
            document.head.appendChild(style);
        })();
    """.trimIndent()

    val REMOVE_DARK_MODE_PAYLOAD = """
        (function() {
            var existingStyle = document.getElementById('nightfall-dark-mode-style');
            if (existingStyle) {
                existingStyle.remove();
            }
        })();
    """.trimIndent()
}
