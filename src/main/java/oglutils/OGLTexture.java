package oglutils;

public interface OGLTexture {
	interface Viewer {
		void view(OGLTexture texture);
		void view(OGLTexture texture, double x, double y);
		void view(OGLTexture texture, double x, double y, double scale);
		void view(OGLTexture texture, double posX, double posY, double scale, double aspectXY);
	}
	void bind(int shaderProgram, String name, int slot);
}
