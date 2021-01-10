package vadiole.boids2d.wallpaper

import android.opengl.GLSurfaceView
import android.view.SurfaceHolder


abstract class OpenGLES2WallpaperService : GLWallpaperService() {
    override fun onCreateEngine(): Engine {
        return OpenGLES2Engine()
    }

    internal inner class OpenGLES2Engine : GLWallpaperService.GLEngine() {
        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            setRenderer(newRenderer)
        }
    }

    abstract val newRenderer: GLSurfaceView.Renderer?
}
