package ul01;

import com.jogamp.opengl.GL4;
import oglutils.OGLBuffers;

public class GridFactory {

    public static OGLBuffers generateGrid(GL4 gl, int rows, int columns) {
        int vertices = rows * columns;
        int triangles = (rows - 1) * (columns - 1) * 2;

        float[] vb = new float[vertices * 2];
        int[] ib = new int[triangles * 3];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                int vbIndex1 = (i * rows + j) * 2;
                int vbIndex2 = (i * columns + j) * 2 + 1;

                vb[vbIndex1] = i / (float) (columns - 1);
                vb[vbIndex2] = j / (float) (rows - 1);
            }
        }
        int index = 0;
        for (int i = 0; i < rows - 1; i++) {
            for (int j = 0; j < columns - 1; j++) {
                ib[index] = j + i * columns;
                ib[index + 1] = j + 1 + (i + 1) * columns;
                ib[index + 2] = j + (i + 1) * columns;
                ib[index + 3] = j + i * columns;
                ib[index + 4] = j + 1 + i * columns;
                ib[index + 5] = j + 1 + (i + 1) * columns;
                index += 6;
            }
        }

        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 2)
        };

        return new OGLBuffers(gl, vb, attributes, ib);
    }
}
