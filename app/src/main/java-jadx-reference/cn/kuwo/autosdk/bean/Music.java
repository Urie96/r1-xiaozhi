package cn.kuwo.autosdk.bean;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.text.TextUtils;
import cn.kuwo.autosdk.bean.DownloadQuality;
import cn.kuwo.autosdk.q;
import cn.kuwo.autosdk.u;
import com.google.android.exoplayer2.text.ttml.TtmlNode;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.io.FilenameUtils;
import org.litepal.util.Const;

/* JADX INFO: loaded from: classes.dex */
public class Music implements Serializable, Cloneable {
    private static final long serialVersionUID = 3985672550071260552L;
    public long artistId;
    public boolean checked;
    public long downSize;
    public int duration;
    private boolean eq;
    public long fileSize;
    private boolean flac;
    public int hasKalaok;
    public boolean hasMv;
    public int hot;
    public boolean playFail;
    public String psrc;
    private Collection resourceCollection;
    public long rid;
    private long storageId;
    public int trend;
    public String name = "";
    public String artist = "";
    public String album = "";
    public String tag = "";
    public String mvQuality = "";
    public String mvIconUrl = "";
    public String source = "";
    public q createDate = new q();
    public LocalFileState localFileState = LocalFileState.NOT_CHECK;
    public String filePath = "";
    public String fileFormat = "";
    public DownloadQuality.Quality downQuality = DownloadQuality.Quality.Q_AUTO;

    public enum LocalFileState {
        NOT_CHECK,
        EXIST,
        NOT_EXIST;

        /* JADX INFO: renamed from: values, reason: to resolve conflict with enum method */
        public static LocalFileState[] valuesCustom() {
            LocalFileState[] localFileStateArrValuesCustom = values();
            int length = localFileStateArrValuesCustom.length;
            LocalFileState[] localFileStateArr = new LocalFileState[length];
            System.arraycopy(localFileStateArrValuesCustom, 0, localFileStateArr, 0, length);
            return localFileStateArr;
        }
    }

    public boolean Contain(Music music) {
        if (music.rid > 0 && this.rid > 0) {
            return music.rid == this.rid;
        }
        if (this.rid <= 0 && music.rid != 0) {
            return false;
        }
        return u.a(music.filePath, this.filePath);
    }

    public boolean addResource(MusicQuality musicQuality, int i, MusicFormat musicFormat, int i2) {
        return addResource(new NetResource(musicQuality, i, musicFormat, i2));
    }

    public boolean addResource(NetResource netResource) {
        if (netResource == null) {
            return false;
        }
        if (this.resourceCollection == null) {
            this.resourceCollection = new ArrayList();
        }
        Iterator it = this.resourceCollection.iterator();
        while (it.hasNext()) {
            if (((NetResource) it.next()).equals(netResource)) {
                return false;
            }
        }
        if (netResource.isEQ()) {
            this.eq = true;
        }
        if (netResource.isFLAC()) {
            this.flac = true;
        }
        return this.resourceCollection.add(netResource);
    }

    /* JADX INFO: renamed from: clone, reason: merged with bridge method [inline-methods] */
    public Music m4clone() {
        try {
            Music music = (Music) super.clone();
            if (this.resourceCollection == null) {
                return music;
            }
            music.resourceCollection = new ArrayList();
            Iterator it = this.resourceCollection.iterator();
            while (it.hasNext()) {
                music.resourceCollection.add(((NetResource) it.next()).m5clone());
            }
            return music;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean equalsEx(Music music) {
        return music.rid > 0 ? music.rid == this.rid : (this.filePath == null || music.filePath == null) ? this.filePath == null && music.filePath == null : this.filePath.equals(music.filePath);
    }

    public NetResource getBestResource() {
        if (this.resourceCollection == null) {
            return null;
        }
        NetResource netResource = null;
        for (NetResource netResource2 : this.resourceCollection) {
            if (netResource == null) {
                netResource = netResource2;
            } else if (netResource.bitrate < netResource2.bitrate) {
                netResource = netResource2;
            }
        }
        return netResource;
    }

    public NetResource getBestResource(MusicQuality musicQuality) {
        if (this.resourceCollection == null) {
            return null;
        }
        NetResource netResource = null;
        for (NetResource netResource2 : this.resourceCollection) {
            if (netResource2.quality.ordinal() <= musicQuality.ordinal() && (netResource == null || netResource.bitrate < netResource2.bitrate)) {
                netResource = netResource2;
            }
        }
        return netResource;
    }

    public boolean getInfoFromDatabase(Cursor cursor) {
        try {
            setStorageId(cursor.getLong(cursor.getColumnIndex(TtmlNode.ATTR_ID)));
            this.rid = cursor.getLong(cursor.getColumnIndex("rid"));
            this.name = u.c(cursor.getString(cursor.getColumnIndex(Const.TableSchema.COLUMN_NAME)));
            this.artist = u.c(cursor.getString(cursor.getColumnIndex("artist")));
            this.artistId = cursor.getLong(cursor.getColumnIndex("artistid"));
            this.album = u.c(cursor.getString(cursor.getColumnIndex("album")));
            this.duration = cursor.getInt(cursor.getColumnIndex("duration"));
            this.hasMv = cursor.getInt(cursor.getColumnIndex("hasmv")) > 0;
            this.mvQuality = u.c(cursor.getString(cursor.getColumnIndex("mvquality")));
            this.hasKalaok = cursor.getInt(cursor.getColumnIndex("haskalaok"));
            this.downSize = cursor.getInt(cursor.getColumnIndex("downsize"));
            this.downQuality = DownloadQuality.Quality.valueOf(u.c(cursor.getString(cursor.getColumnIndex("downquality"))));
            this.filePath = u.c(cursor.getString(cursor.getColumnIndex("filepath")));
            this.fileSize = cursor.getLong(cursor.getColumnIndex("filesize"));
            this.fileFormat = u.c(cursor.getString(cursor.getColumnIndex("fileformat")));
            if (cursor.getColumnIndex("resource") >= 0) {
                parseResourceStringFromDatabase(u.c(cursor.getString(cursor.getColumnIndex("resource"))));
            }
            if (cursor.getColumnIndex("createtime") < 0) {
                this.createDate = new q();
                return true;
            }
            String strC = u.c(cursor.getString(cursor.getColumnIndex("createtime")));
            if (TextUtils.isEmpty(strC)) {
                this.createDate = new q();
                return true;
            }
            this.createDate = new q(strC);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ContentValues getMusicContentValues(long j) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("rid", Long.valueOf(this.rid));
        contentValues.put("listid", Long.valueOf(j));
        contentValues.put(Const.TableSchema.COLUMN_NAME, u.c(this.name));
        contentValues.put("artist", u.c(this.artist));
        contentValues.put("artistid", Long.valueOf(this.artistId));
        contentValues.put("album", u.c(this.album));
        contentValues.put("duration", Integer.valueOf(this.duration));
        contentValues.put("hot", Integer.valueOf(this.hot));
        contentValues.put("source", u.c(this.source));
        contentValues.put("resource", u.c(getResourceStringForDatabase()));
        contentValues.put("hasmv", Integer.valueOf(this.hasMv ? 1 : 0));
        contentValues.put("mvquality", u.c(this.mvQuality));
        contentValues.put("haskalaok", Integer.valueOf(this.hasKalaok));
        contentValues.put("downsize", Long.valueOf(this.downSize));
        contentValues.put("downquality", this.downQuality == null ? "" : this.downQuality.toString());
        contentValues.put("filepath", u.c(this.filePath));
        contentValues.put("fileformat", u.c(this.fileFormat));
        contentValues.put("filesize", Long.valueOf(this.fileSize));
        contentValues.put("createtime", u.c(this.createDate.a()));
        return contentValues;
    }

    public NetResource getResource(MusicFormat musicFormat) {
        if (this.resourceCollection == null) {
            return null;
        }
        NetResource netResource = null;
        for (NetResource netResource2 : this.resourceCollection) {
            if (netResource2.format == musicFormat && (netResource == null || netResource.bitrate < netResource2.bitrate)) {
                netResource = netResource2;
            }
        }
        return netResource;
    }

    public NetResource getResource(MusicQuality musicQuality) {
        if (this.resourceCollection == null) {
            return null;
        }
        NetResource netResource = null;
        for (NetResource netResource2 : this.resourceCollection) {
            if (netResource2.quality == musicQuality && (netResource == null || netResource.bitrate < netResource2.bitrate)) {
                netResource = netResource2;
            }
        }
        return netResource;
    }

    public Collection getResourceCollection() {
        return this.resourceCollection;
    }

    public String getResourceStringForDatabase() {
        if (this.resourceCollection == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (NetResource netResource : this.resourceCollection) {
            sb.append(netResource.quality.getDiscribe()).append(".").append(netResource.bitrate).append(".");
            sb.append(netResource.format.getDiscribe()).append(".").append(netResource.size).append(";");
        }
        return sb.toString();
    }

    public long getStorageId() {
        return this.storageId;
    }

    public boolean hasHighMv() {
        if (!this.hasMv || this.mvQuality == null) {
            return false;
        }
        for (String str : u.a(this.mvQuality, ';')) {
            if (str.equalsIgnoreCase("MP4")) {
                return true;
            }
        }
        return false;
    }

    public boolean hasLowMv() {
        if (!this.hasMv || this.mvQuality == null) {
            return false;
        }
        for (String str : u.a(this.mvQuality, ';')) {
            if (str.equalsIgnoreCase("MP4L")) {
                return true;
            }
        }
        return false;
    }

    public int hashCodeEx() {
        if (this.rid > 0) {
            return (int) this.rid;
        }
        if (this.filePath == null) {
            return 0;
        }
        return this.filePath.hashCode();
    }

    public boolean isEQ() {
        return this.eq;
    }

    public boolean isFLAC() {
        return this.flac;
    }

    public boolean isLocalFile() {
        return this.rid <= 0;
    }

    public int parseResourceStringFromDatabase(String str) {
        if (TextUtils.isEmpty(str)) {
            return 0;
        }
        int i = 0;
        for (String str2 : u.a(str, ';')) {
            if (!TextUtils.isEmpty(str2)) {
                String[] strArrA = u.a(str2, FilenameUtils.EXTENSION_SEPARATOR);
                if (strArrA.length == 4) {
                    try {
                        if (addResource(new NetResource(MusicQuality.getQualityFromDiscribe(strArrA[0]), Integer.valueOf(strArrA[1]).intValue(), MusicFormat.getFormatFromDiscribe(strArrA[2]), Integer.valueOf(strArrA[3]).intValue()))) {
                            i++;
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return i;
    }

    /* JADX WARN: Removed duplicated region for block: B:39:0x00c4  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public int parseResourceStringFromQuku(String str) {
        int i;
        int i2;
        if (TextUtils.isEmpty(str)) {
            return 0;
        }
        String[] strArrA = u.a(str, ';');
        int length = strArrA.length;
        int i3 = 0;
        int i4 = 0;
        while (i3 < length) {
            String[] strArrA2 = u.a(strArrA[i3], ',');
            if (strArrA2.length == 4) {
                String strB = u.b(strArrA2[0]);
                String strB2 = u.b(strArrA2[1]);
                String strB3 = u.b(strArrA2[2]);
                String strB4 = u.b(strArrA2[3]);
                MusicQuality qualityFromDiscribe4Quku = MusicQuality.getQualityFromDiscribe4Quku(strB);
                int i5 = u.a(strB2) ? Integer.parseInt(strB2) : 0;
                MusicFormat formatFromDiscribe4Quku = MusicFormat.getFormatFromDiscribe4Quku(strB3);
                if (strB4.toUpperCase().indexOf("KB") > 0) {
                    try {
                        i2 = (int) (Float.parseFloat(strB4.replaceAll("(?i)kb", "")) * 1024.0f);
                    } catch (Exception e) {
                        e.printStackTrace();
                        i2 = 0;
                    }
                } else if (strB4.toUpperCase().indexOf("MB") > 0) {
                    try {
                        i2 = (int) (Float.parseFloat(strB4.replaceAll("(?i)mb", "")) * 1024.0f * 1024.0f);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                        i2 = 0;
                    }
                } else if (strB4.toUpperCase().indexOf("B") > 0) {
                    try {
                        i2 = (int) Float.parseFloat(strB4.replaceAll("(?i)b", ""));
                    } catch (Exception e3) {
                        e3.printStackTrace();
                        i2 = 0;
                    }
                } else {
                    i2 = 0;
                }
                i = addResource(new NetResource(qualityFromDiscribe4Quku, i5, formatFromDiscribe4Quku, i2)) ? i4 + 1 : i4;
            }
            i3++;
            i4 = i;
        }
        return i4;
    }

    public void setLocalFileExist(boolean z) {
        this.localFileState = z ? LocalFileState.EXIST : LocalFileState.NOT_EXIST;
    }

    public void setResourceCollection(Collection collection) {
        this.resourceCollection = collection;
    }

    public void setStorageId(long j) {
        if (0 > j) {
            return;
        }
        this.storageId = j;
    }

    public String toDebugString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name:").append(this.name);
        sb.append(", Artist:").append(this.artist);
        sb.append(", Album:").append(this.album);
        sb.append(", Rid:").append(this.rid);
        sb.append(", Path:").append(this.filePath);
        return sb.toString();
    }

    public boolean vaild() {
        return this.rid > 0 || !TextUtils.isEmpty(this.filePath);
    }
}
