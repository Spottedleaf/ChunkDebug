package ca.spottedleaf.chunkdebug.ui;

import ca.spottedleaf.chunkdebug.data.ChunkInformation;
import ca.spottedleaf.chunkdebug.data.ServerInformation;

import javax.swing.*;

public final class RegionUI extends JPanel {



    static void init(final ServerInformation info) {

    }

    static final class ChunkPanel extends JPanel {

        public static final ChunkPanel EMPTY_PANEL = new ChunkPanel(null);

        public final ChunkInformation chunk;

        ChunkPanel(final ChunkInformation chunk) {
            this.chunk = chunk;
        }
    }

}