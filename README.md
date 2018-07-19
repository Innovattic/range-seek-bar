# RangeSeekBar

The missing view for android.

- [Usage](#usage)
- [Attributes](#attributes)

### Usage
First add jitpack to your projects build.gradle file

```gradle
allprojects {
   	repositories {
   		...
   		maven { url "https://jitpack.io" }
   	}
}
```

Then add the dependency in your android app module's `build.gradle` file.

```gradle
dependencies {
    compile 'com.github.innovattic:range-seek-bar:1.0'
}
```

Then use the view in your layouts:

```xml
<com.innovattic.rangeseekbar.RangeSeekBar
    android:id="@+id/rangeSeekBar"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="@color/colorAccent" />
```


### Attributes

You can change view attributes directly from your layout's xml file or in your java/kotlin code:

| Variable                   | XML Attribute              | Type      | Description                                                                            |
| :------------------------- | :------------------------- | :-------- | :--------------------------------------------------------------------------------------|
| trackColor                 | rsb_trackColor             | color     | Color of horizontal track                                                              |
| trackSelectedColor         | rsb_trackSelectedColor     | color     | Color of the selected range of horizontal track                                        |
| trackThickness             | rsb_trackThickness         | dimension | The thickness of the horizontal track                                                  |
| trackSelectedThickness     | rsb_trackSelectedThickness | dimension | The thickness of the selected range of horizontal track                                |
| sidePadding                | rsb_sidePadding            | dimension | Side padding for view, by default 16dp on the left and right                           |
| touchRadius                | rsb_touchRadius            | dimension | The acceptable touch radius around thumbs in pixels                                    |
| minThumbDrawable           | rsb_minThumbDrawable       | reference | The drawable to draw min thumb with                                                    |
| maxThumbDrawable           | rsb_maxThumbDrawable       | reference | The drawable to draw max thumb with                                                    |
| minRange                   | rsb_minRange               | integer   | The minimum range to be selected. It should at least be 1                              |
| max                        | rsb_max                    | integer   | The maximum value of thumbs which can also be considered as the maximum possible range |