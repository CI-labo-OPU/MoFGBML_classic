# MoFGBML_ver22

## Release Note

### SubdivisionMOP_ver2 -

 + カテゴリカル属性に対するメンバシップ関数をシングルトン的に出力するように変更．

 カテゴリカル属性は，現状では，負の整数で表現される．
 また，カテゴリカル属性に対するファジィ集合も同様に負の整数で表現され，
 メンバシップ関数はシングルトンであり，ファジィ集合と入力値が等しければ1.0，異なれば0.0を出力する．
 カテゴリカル属性のヒューリスティック生成は，ランダムに選択したパターンのカテゴリ値を条件部とする．
 カテゴリカル属性の突然変異は，学習用データからランダムに選択したパターンのカテゴリ値を条件部とする．

 + class-entropy based partitioning method の追加

 合わせ実験用として，dataset/partition/partition.datを使用する．
 文献「Comparison between Fuzzy and Interval Partitions in Evolutionary Multiobjective Design of Rule-Based Classification Systems」のFig. 2. のデータセット．

