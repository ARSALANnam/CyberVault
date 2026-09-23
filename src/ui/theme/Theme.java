package ui.theme;

import java.awt.Color;

public class Theme {
    public final String name, displayName;
    public final Color bg, bgPanel, bgCard, bgField;
    public final Color line, neonCyan, neonPink, neonPurp, neonGrn, neonYel;
    public final Color txt, txtDim, bgGrad, dim1, dim2, scrollC;
    public final boolean matrixRain;

    public Theme(String name, String displayName,
                 Color bg, Color bgPanel, Color bgCard, Color bgField,
                 Color line, Color neonCyan, Color neonPink, Color neonPurp, Color neonGrn, Color neonYel,
                 Color txt, Color txtDim, Color bgGrad, Color dim1, Color dim2, Color scrollC,
                 boolean matrixRain) {
        this.name = name; this.displayName = displayName;
        this.bg = bg; this.bgPanel = bgPanel; this.bgCard = bgCard; this.bgField = bgField;
        this.line = line; this.neonCyan = neonCyan; this.neonPink = neonPink;
        this.neonPurp = neonPurp; this.neonGrn = neonGrn; this.neonYel = neonYel;
        this.txt = txt; this.txtDim = txtDim; this.bgGrad = bgGrad;
        this.dim1 = dim1; this.dim2 = dim2; this.scrollC = scrollC;
        this.matrixRain = matrixRain;
    }
}
