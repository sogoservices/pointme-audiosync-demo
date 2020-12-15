package com.sogoservices.pointme.demo;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.sogoservices.pointme.sdk.data.PNMPublicPoint;

import java.util.Objects;

public class SearchResult implements Parcelable {

    public static final Creator<SearchResult> CREATOR = new Creator<SearchResult>() {
        @Override
        public SearchResult createFromParcel(Parcel in) {
            return new SearchResult(in);
        }

        @Override
        public SearchResult[] newArray(int size) {
            return new SearchResult[size];
        }
    };

    public String uuid;
    public PNMPublicPoint.Type type;
    public String description;
    public Bitmap bitmap;
    public String url;

    protected SearchResult(Parcel in) {
        uuid = in.readString();
        type = (PNMPublicPoint.Type) in.readSerializable();
        description = in.readString();
        bitmap = in.readParcelable(Bitmap.class.getClassLoader());
        url = in.readString();
    }

    public SearchResult(String uuid, PNMPublicPoint.Type type, String description, Bitmap bitmap, String url) {
        this.uuid = uuid;
        this.type = type;
        this.description = description;
        this.bitmap = bitmap;
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchResult that = (SearchResult) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uuid);
        dest.writeSerializable(type);
        dest.writeString(description);
        dest.writeParcelable(bitmap, flags);
        dest.writeString(url);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "{" +
            "uuid='" + uuid + '\'' +
            ", description='" + description + '\'' +
            ", url='" + url + '\'' +
            '}';
    }
}
