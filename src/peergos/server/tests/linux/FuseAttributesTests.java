package peergos.server.tests.linux;

import jnr.posix.POSIX;
import jnr.posix.POSIXFactory;
import org.junit.Test;
import peergos.server.fuse.PeergosFS;
import peergos.shared.user.TrieNodeImpl;
import peergos.shared.user.fs.FileWrapper;
import ru.serce.jnrfuse.struct.FileStat;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class FuseAttributesTests {
    @Test
    public void getattrUsesMountProcessOwnership() throws Exception {
        FileWrapper root = FileWrapper.createRoot(TrieNodeImpl.empty());
        POSIX posix = POSIXFactory.getPOSIX();
        FileStat attributes = new FileStat(jnr.ffi.Runtime.getSystemRuntime());
        attributes.st_uid.set(-1);
        attributes.st_gid.set(-1);

        try (PeergosFS filesystem = new PeergosFS(null) {
            @Override
            protected Optional<PeergosStat> getByPath(String path) {
                return Optional.of(new PeergosStat(root, root.getFileProperties()));
            }
        }) {
            assertEquals(0, filesystem.getattr("/", attributes));
            assertEquals(posix.getuid(), attributes.st_uid.get());
            assertEquals(posix.getgid(), attributes.st_gid.get());
        }
    }
}
