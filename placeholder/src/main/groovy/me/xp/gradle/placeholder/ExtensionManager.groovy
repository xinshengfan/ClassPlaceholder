package me.xp.gradle.placeholder

class ExtensionManager {
    List<PlaceholderExtension> extensions = []

    void cacheExtensions(List<PlaceholderExtension> extensions) {
        this.extensions.clear()
        this.extensions.addAll(extensions)
    }

    static ExtensionManager instance() {
        return Single.instance
    }

    private static class Single {
        private static ExtensionManager instance = new ExtensionManager()
    }
}