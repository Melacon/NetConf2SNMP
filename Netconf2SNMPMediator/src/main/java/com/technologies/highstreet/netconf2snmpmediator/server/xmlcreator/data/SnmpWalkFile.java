package com.technologies.highstreet.netconf2snmpmediator.server.xmlcreator.data;

import com.google.common.io.Files;
import com.technologies.highstreet.mediatorlib.data.DataTable;
import com.technologies.highstreet.mediatorlib.netconf.server.basetypes.SnmpKeyValuePair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SnmpWalkFile {

    private static final Log LOG = LogFactory.getLog(SnmpWalkFile.class);

    public static boolean AUTOBACKUP = true;
    public static boolean PRELOAD = true;
    private static final String LINEFORMAT = "%s(%s):%s";
    private final BufferedWriter fw;
    private List<SnmpKeyValuePair> mValues;
    private final String mFilename;

    public String getLastOid() {
        if (this.mValues == null || this.mValues.size() <= 0) {
			return "0.0";
		}
        return this.mValues.get(this.mValues.size() - 1).getOid();
    }

    public HashMap<String, String> getValueListForOid(String oidPrefix) {
        HashMap<String, String> res = new HashMap<>();
        if (oidPrefix == null || oidPrefix.isEmpty()) {
            throw new IllegalArgumentException("Invalid table oid: Empty or null");
        }

        if (!oidPrefix.endsWith(".")) {
            oidPrefix = oidPrefix + ".";
        }

        // System.out.println("Table for oid "+oidPrefix);
        for (SnmpKeyValuePair keyValue : mValues) {
            String oidKey = keyValue.getOid();
            if (oidKey.startsWith(oidPrefix)) {
                String key = oidKey.substring(oidPrefix.length());
                if (!key.isEmpty()) {
                    // System.out.println(">> "+key+"/"+keyValue.Value);
                    res.put(key, keyValue.getValue());
                }
            }
        }
        return res;

    }

    /*
     * prefill String with character to length
     */
    public static String strPad(String s, int length, char fill) {
        if (s != null) {
            while (s.length() < length) {
				s = Character.toString(fill) + s;
			}
        }
        return s;
    }

    public DataTable getTable(String baseOID, int cols[]) {
        DataTable dt = new DataTable();
        for (int col : cols) {
            dt.AddColumn(String.format("%d", col));
        }
        for (SnmpKeyValuePair x : mValues) {
            if (x.getOid().startsWith(baseOID)) {
                // System.out.println("handle "+x.OID + " value="+ x.Value);
                String sub = x.getOid().substring(baseOID.length() + 1);
                int xcol = Integer.parseInt(sub.substring(0, sub.indexOf('.')));
                sub = sub.substring(sub.indexOf('.') + 1);
                int xrow;
                if (sub.contains(".")) {
                    xrow = Integer.parseInt(sub.substring(0, sub.indexOf('.')));
                    String rowIndex = sub.substring(sub.indexOf('.') + 1);
                    dt.setValueAt(xcol, xrow, rowIndex, x.getValue());
                } else {
                    xrow = Integer.parseInt(sub);
                    dt.setValueAt(xcol, xrow, x.getValue());
                }

            }
        }
        return dt;
    }

    public SnmpWalkFile(String filename) throws IOException {
        this.mFilename=filename;
        if (AUTOBACKUP) {
            LOG.debug("create backup:" + filename + " to " + filename + ".bak");
            File file=new File(filename);
            if(file.exists()) {
				Files.copy(new File(filename), new File(filename + ".bak"));
			}

        }
        if (PRELOAD) {
            LOG.debug("preloading walkfile");

            this.read(filename);
        }
        this.fw = new BufferedWriter(new FileWriter(filename, PRELOAD));
    }
    public void removeBase(String baseOid)
    {
        if(this.mValues==null) {
			this.mValues = new ArrayList<>();
		}
        ListIterator<SnmpKeyValuePair> it=this.mValues.listIterator();
        while(it.hasNext())
        {
            if(it.next().getOid().startsWith(baseOid)) {
				it.remove();
			}
        }
    }
    public void reload() throws IOException
    {
        this.reload(this.mFilename);
    }
    public void reload(String filename) throws IOException
    {
        if(this.mValues!=null) {
			this.mValues.clear();
		}
        this.read(filename);
    }
    public void add(SnmpKeyValuePair x)
    {
        if(this.mValues==null) {
			this.mValues = new ArrayList<>();
		}
        this.mValues.add(x);
    }
    private void read(String filename) throws IOException {
        if (this.mValues == null) {
			this.mValues = new ArrayList<>();
		}
        File file=new File(filename);
        if(!file.exists())
        {
            LOG.debug("file doesnt exists");
            return;
        }
        BufferedReader rd = new BufferedReader(new FileReader(filename));
        String line;
        this.mValues.clear();
        final String regex = "([\\.\\d]+)\\((\\d)\\)\\:(.*)";
        final Pattern pattern = Pattern.compile(regex);
        Matcher matcher;
        LOG.debug("start reading file " + filename);
        int i = 0, valids = 0;
        while ((line = rd.readLine()) != null) {
            if (line.length() > 0) {
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    valids++;
                    this.mValues.add(new SnmpKeyValuePair(matcher.group(1), matcher.group(3),
                            Integer.parseInt(matcher.group(2))));
                }
            }
            i++;
            if (i % 50 == 0) {
                LOG.debug("line " + i + " valids: " + valids);
            }
        }
        rd.close();
        LOG.debug("finished with " + this.mValues.size() + " items");
    }

    public void write(SnmpKeyValuePair v) throws IOException {
        if (fw != null) {
			fw.write(String.format(LINEFORMAT + "\n", v == null ? "null" : v.getOid(), v == null ? "null" : v.getValueType(),
                    v == null ? "null" : v.getValue()));
		}
    }

    public void close() throws IOException {
        if (fw != null) {
            fw.flush();
            fw.close();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (fw != null) {
            fw.flush();
            fw.close();
        }
        super.finalize();
    }

}
