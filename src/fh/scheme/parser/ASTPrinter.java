package fh.scheme.parser;

import java.util.Formatter;
import java.util.List;

public class ASTPrinter {
    public static void traverse(String destruct, Entry e) {
        if (e != null) {
            if (destruct.length() == 0) {
                System.out.printf("%s : %s\n", destruct, e.getToken());
            }
            if (e.getChildren() != null) {
                List<Entry> children = e.getChildren();
                for (int i = 0; i < children.size(); i++) {
                    Entry g = children.get(i);
                    System.out.printf("%s.getChildren.get(%d) : %s\n",
                            destruct, i, g);
                    if (g.getChildren() != null) {
                        Formatter f = new Formatter();
                        f.format(".getChildren.get(%d)", i);
                        traverse(destruct + f.toString(), g);
                    }
                }
            }
        }
    }

    public static String getEntryAsString(Entry e) {
        StringBuilder ret = new StringBuilder();
        if (e != null) {
            ret.append(e.getToken().getText()+" ");
            if (e.getChildren() != null) {
                List<Entry> children = e.getChildren();
                for (int i = 0; i < children.size(); i++) {
                    Entry g = children.get(i);
                    ret.append(getEntryAsString(g));
                }
            }
        }
        return ret.toString();
    }
}
