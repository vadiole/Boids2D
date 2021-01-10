package vadiole.boids2d.wallpaper

import android.opengl.GLSurfaceView
import vadiole.boids2d.boids.BoidsRenderer


class BoidsWallpaperService : OpenGLES2WallpaperService() {
    override val newRenderer: GLSurfaceView.Renderer
        get() = BoidsRenderer(this)
}
