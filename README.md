# PhotoSlideShow

## Overview

Googleフォトで共有したアルバム内の写真をスライドショーしてくれるアプリです。時間系列直近の写真100枚の中からランダムにピックアップして、画像を表示します。  

単純にローカルに置いた画像を表示するアプリとは違いGoogleアカウントでフォトと連携するため、
他の共有者が適当なタイミングで画像をアップロードしても手動でローカル上にダウンロードする必要なく表示してくれます。

現在は「共有」カテゴリ内のアルバムだけ選択できますが、
今後の開発で「アルバム」カテゴリも選択できるようにすることも検討中です。

## How to use?

1. アプリを初回起動するとスプラッシュ後にパーミッション許可の確認画面が表示されるので許可してください。（Android 6 未満はパーミッション許可がでません。）
1. Googleアカウントサインイン画面がでるので、サインインしてください。（大抵は端末でサインインしているGoogleアカウントに選択することになると思います。）
1. サインインしてしばらくすると、フォトの共有内にあるアルバムの一覧が表示されます。アプリでスライドショーしたいアルバムを選択してください。
1. しばらくして読み込み完了すると画像が表示されます。

## Library mainly used
- Google Sign-in API
  - Googleアカウントサインインに OAuth 2.0 を利用
  - アカウント情報や後述のフォトライブラリで使うトークンを取得する
  - https://developers.google.com/identity/sign-in/android/
- Google Photo API
  - Googleフォトからアルバムや画像の取得を取得する
  - https://developers.google.com/photos/
  - https://google.github.io/java-photoslibrary/1.3.0/
- OSS Licenses Gradle Plugin
  - 使用しているオープンソースライブラリのライセンス表示のため使用
  - https://developers.google.com/android/reference/com/google/android/gms/oss/licenses/package-summary
  - https://github.com/google/play-services-plugins/tree/master/oss-licenses-plugin
  - このライブラリを適用すると GradleSync時にWarningが出る。ライブラリ修正され次第アップデートする
    - https://github.com/google/play-services-plugins/issues/57

## Todo list for developer

### 課題
- 共有アルバム内の画像が多いと読み込みに時間がかかる、もうちょっとはやくできないものか？（ライブラリとの兼ね合い、技術検討）
- アルバムの選択がサインイン直後のみ、サインインしたまま選択できるようにしたい（設計の見直し）
- 「共有」内のアルバムのみ選択可、他も選択できるようにしたい（ライブラリとの兼ね合い、技術検討）
- 動画を流すことはできないか？（技術検討）
- 時間系列直近の100の中からピックアップしているが、古い画像もある程度ピックアップできないか？（アルゴリズム検討）
  - 時間系列が新しいものほど表示割合が多く、古いものほど割合を低くしたい
  - Ex. １ヶ月以内の写真：半年以内の写真：それ以前の写真 ＝ 5:3:2
- アプリアイコンどうにかしたい、、、

### 直近で入れたい機能
- パッケージ名の変更
- Boltsフレームワークの導入
- アニメーション
- 読み込み中の体裁
- ライブラリのアップデート

### 後回し機能
- アップデート機能
- デザイン
- 画面回転考慮（タブレットのみ対応）

### Refactoring
- Java8 lambda (list filer)
  - https://qiita.com/tasogarei/items/60b5e55d8f42732686c6
- パッケージ分け(Release, debug)
- DialogFragmentの改善
- config password in gradlew assembleRelease (build.gradle)
