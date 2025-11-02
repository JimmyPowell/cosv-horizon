# COSV-Schema 1 .0. 0 标准说明

# 1 设计背景

### 为进一步增强漏洞描述的准确性、规范性及全面性，为上层漏洞治理平台提供标准化

### 数据结构便于跨系统无缝流转，CCF开源发展委员会（简称CCF ODC）开源供应链安全工

### 作组响应行业多部门共性诉求，近期组织业界同行共同研讨优化并制定出CCF版开源漏洞

信息描述规范CCF Open Source Vulnerability (COSV) Schema 1.0.0并正式对外发布。该文档

针对COSV- Schema 1 .0. 0 标准细节进行相关说明。

# 2 设计原则

### 1. 合规性原则：需遵循相关行业指导部门颁布的软件供应链安全相关法律法规，如

### 2021 年工信部联合网信办、公安部颁布的《网络产品漏洞管理规定》[1]， 2022 年国家标

### 准《信息安全技术 软件供应链安全要求》（征求意见稿）等。

2. 兼容性原则：鉴于OpenSSF发布的OSV-Schema[2]已在国际开源漏洞库得到较为广泛
的使用，COSV-Schema的字段名与含义应尽量与OSV-Schema兼容，以保证业界开源漏
洞库信息可无缝导入。
3. 最小功能原则：如无必要，勿增实体。相对现有字段，COSV-Schema只增加具备足
够必要性的重要字段。
4. 单一职责原则：在拓展已有对象内部字段时，应保证该字段信息属于当前对象的正
常功能职责范围。
5. 通用性原则：尽量使用抽象的字段名，以支持更多类型特定信息的描述，比如为了
描述水果价格，建议按照如下方式来定义：{“fruit_name”: “apple”, “price”: 10} （可支持
多种类型水果价格的描述，该定义方式具有较好的通用性），而不是 {“apple_price”:
10} （仅支持单一水果价格，缺乏通用性）。
6. 规范命名原则：字段命名应遵循开源的json-api v1.1[3]，如：使用小写的英文单词，
单词间用下划线连接，一个字段名建议不超过 2 个单词，列表类型命名建议使用单词的
复数形式。
7. 最小集原则：明确可选字段和必选字段，在漏洞报告的职责范围内提供字段的最小
集。


### 8. 拓展机制：预留可扩展字段，字段内不作格式限制。

# 3 COSV-Schema v1.0. 0 标准解读

相比OSV-Schema, 新增部分使用蓝色字体进行标记，必选字段使用黄色高亮

 id: String, 数据库唯一索引标识，建议格式为{DB}-{YEAR}-{ID}，有益于不同库之间
的数据交换；an identifier could be used as unique index in database, the format {DB}-
{YEAR}-{ID} like CNVD- 2023 - 12345 is recommended, which is helpful for data exchange

 aliases[]: List of String, 当前漏洞在其它漏洞库中对应的一个或多个id；Records the
ID(s) of the vulnerability item in other vulnerability databases.

 related[]: List of String, 与当前漏洞关系密切相关的漏洞ID(s)(如同一个问题可能在不同
ecosystem中多次出现)；gives a list of IDs of closely related vulnerabilities, such as the
same problem in alternate ecosystems.

 schema_version: String, 当前漏洞报告所采用的COSV-schema版本号；the version
number of COSV-Schema the current entry is specified with.

 cwe_ids[]: List of String, 漏洞类型对应的公共缺陷枚举ID(s); Common Weakness
Enumerate[3] IDs of this vulnerability item.

 cwe_names[]: List of String, 漏洞类型对应的公共缺陷枚举名，Common Weakness
Enumerate names of this vulnerability item.

 time_line[]: List of JSON object, 该时间线用于记录这个漏洞本身的各个生命周期对应
时间点，请注意该时间线与当前漏洞信息报告的发布或撤销时间不同（该schema中的
“published” 、“withdrawn”等字段用来记录每条漏洞报告的发布时间及撤回时间等）；
the life cycle of the vulnerability itself, this should be distinguished from “published” or
“withdrawn” which describes time points of this vulnerability entry not the vulnerability
itself.
 time_line[].type: String，时间线上时间点的类型，包括但不限于: “introduced”,
“found”, “fixed”, “disclosed”; type of time point
 time_line[].value: String, 时间点的值，符合UTC的时间戳；an RFC3339-formatted
time stamp in UTC (ending in “Z”), e.g. “2023- 03 - 13T16:43Z”

 summary: String, 漏洞描述简要概括（限制在一行内）；a brief introduction of
vulnerability within one line

 details: String, 关于漏洞的详细的文本描述，可以使用markdown格式；detailed
description in markdown format

 references[]: List of JSON object, 漏洞相关链接，需指出每个url的类型；specifies the
type of each related url:


```
 references[].type: String，链接类型，如“patch”、“advisory”；the url type
 references[].url: String，链接地址; url
```
 published: String, 当前漏洞报告的发布时间，遵循RFC3339格式的UTC时间戳；the
entry should be considered to have been published, as an RFC3339-formatted time stamp in
UTC (ending in “Z”).

 withdrawn: String, 当前漏洞报告被撤回的时间，如果没有被撤回则此字段为空；an
RFC3339-formatted timestamp in UTC (ending in “Z”). If the field is missing, then the entry
has not been withdrawn

 modified: String, 当前漏洞报告最近一次修改的时间，没有被修改时就等于发布时间；
gives the time the entry was last modified, as an RFC3339-formatted timestamp in UTC
(ending in “Z”)

 severity[]: List of JSON object, 支持多种版本的危险性评级，gives multiple versions of
severity
 severity[].type: String, 指出当前危险性评级所使用的量化方法，比如CVSS；describes
the quantitative method used to calculate the associated score, e.g., CVSS
 severity[].score: String, 当前危险性评级给出的攻击向量评分；based on the selected
severity[].type, e.g. “CVSS:3.1/AV:N/AC:L/PR:L/UI:N/S:U/C:H/I:H/A:H”
 severity[].level: String, 当前危险程度对应的枚举级别，比如低危、中危、高危和严重
gives the Enum of danger or criticalness, e.g., Low, Medium, High, Critical
 severity[].score_num: String, 当前危险性对应的基础评分，可通过上述severity[].score
计算得出，取值范围以及计算规则由severity[].type定义，比如CVSS 3 是 0 ~10之
间的保留一位小数的数字；gives the base score num, calculated based on
severity[].score, the value and calculation rules are defined by severity[].type. e.g, for
CVSS3 score, the value is between 0 and 10 with one decimal place.

 affected[]: List of JSON object, 受漏洞影响的详情；the affection info of this vuln item.

```
 affected[].package: JSON object: 受漏洞影响的包详情; the detail info of each affected
package of this vuln item.
 affected[].package.ecosystem: String，包所属生态系统；the ecosystem the affected
package is belonging to
 affected[].package.name: String，受影响的包名；the name of the affected package
 affected[].package.purl: String，包名的唯一索引; the unique index of the affected
package, e.g., the package url can be used as an unique index to distinguish a package
 affected[].package.language: String，当前包或软件的主要语言, main developing
language of this package
 affected[].package.repository: String，开源软件的代码仓地址; the link of the open
```

```
source repository of the package
 affected[].package.introduced_commits[]: List of String, 漏洞引入的commit id; the
introducing commits of this vulnerability
 affected[].package.fixed_commits[]: List of String, 用来修复当前漏洞的一个或多
个Commit ID(s); the fixing commit(s) of this vulnerability
 affected[].package.home_page: String，开源软件/包对应的官方主页, home page
of the package
 affected[].package.edition: String，开源软件的发行版名称; the distribution edition
name of package, e.g., Alpine, Debian
```
 affected[].ranges[]: List of Objects, 受影响软件/包的范围; the affected version ranges of
the affected software or library.
 affected[].ranges[].type: String, 字段值是枚举类型：“ECOSYSTEM”, “GIT”,
“SEMVER”，用来指定受影响版本范围的类型。“ECOSYSTEM”表示受影响
软件包的版本使用包管理系统（如Maven、PyPI）进行管理，版本名是特定的
字符串，使用此类型时建议提供显示的受影响版本枚举列表。“GIT”表示引
入和修复的版本是完整的Git提交哈希，需要结合代码仓提交图来确定受影响范
围版本范围。“SEMVER”表示引入和修复版本是SemVer2.0.0[4]定义的语义版
本。The value of this filed is enumeration type and should be one of following:
“ECOSYSTEM”, “GIT” or “SEMVER”. It specifies the type of version range being
recorded. “ECOSYSTEM”: the versions introduced and fixed are arbitrary,
uninterpreted strings specific to the package ecosystem. “GIT”: the versions
introduced and fixed are full-length Git commit hashes. The repository’s commit
graph is needed to evaluate whether a given version is in the range. “SEMVER”: the
versions introduced and fixed are semantic versions as defined by SemVer 2.0.0.
Ranges listed with type SEMVER should not overlap.
 affected[].ranges[].repo: String，代码仓地址；the URL of the package’s code
repository
 affected[].ranges[].events[]: List of JSON object, 用于表述范围的区间端点,每个元
素只能代表一个端点("introduced", "fixed", "last_affected", "limit"其中的一种)，
比如[{"introduced": "1.0.0"}, {"fixed": "1.0.2"}]是合法的，而 {"introduced":
"1.0.0", "fixed": "1.0.2"} 是非法的 ; the interval endpoint used to describe the
range, only a single type (either "introduced", "fixed", "last_affected", "limit") is
allowed in each event object. For instance, {"introduced": "1.0.0", "fixed":
"1.0.2"} is invalid
 affected[].ranges[].events[].introduced: String, 受影响的起始版本，如果是
0 ，表示任意最早版本都会受影响；the first known affected version, if it’s
value is “0” it represent that every known version of package before fixed
version was affected.


```
 affected[].ranges[].events[].fixed: String , 修复该漏洞的最早版本；earliest
version where the vulnerability is fixed
 affected[].ranges[].events[].last_affected: String, 最后受影响的版本；the
last known affected version
 affected[].ranges[].events[].limit: String, 字段值含义为修复提交的节点，
适用于ranges[].type为“GIT”的场景，当指定introduced: A, limit: B时，
表示使用git rev-list A..B命令得到的A B节点之间（包括A，不包括B）所
有的节点都受到影响，如果这个字段的值是*，表示无穷，如果没有给出
一个limit值，默认有一个值为*的limit存在。This field indicates the
submission node of the fix, which is used in the Git submission scenario.
When given introduced: A, limit: B, indicates the commit nodes of git rev-list
A..B are all affected. It allows versions containing the string “*” , to represent
“infinity”. If no limit events are provided, an implicit {“limit”: “*”} is
assumed to exist
 affected[].ranges[].database_specific: JSON object，一些漏洞库可以提供关
于受影响范围的特定内容，比如受影响范围的数学区间表示。Databases
may provide specific descriptions about affected ranges, like interval string
e.g. “(0, 1.2.1]”
 affected[].versions[]: String, 漏洞的受影响版本枚举，每个元素代表一个明确的受影
响版本。Each string is a single affected version in whatever version syntax is used by
the given package ecosystem.
```
 patches_detail[]: List of JSON object，本次漏洞修复补丁相关的详细信息, the detail info
of the fix patch of each vulnerability.
 patches_detail[].patch_url: String，补丁链接, the URL of fix patch
 patches_detail[].issue_url: String, 当前补丁涉及的问题单地址, the URL of the issue
 patches_detail[].main_language: String，本次漏洞涉及的主要编程语言, the main code
language of this vulnerability
 patches_detail[].author: String，修复提交的作者; the author of the fix commit
 patches_detail[].commiter: String，commiter相关信息; the commiter of the fix commit
 patches_detail[].branches[]: List of String，修复提交涉及的分支名; the branch names
that this fixing patch is committed into.
 patches_detail[].tags[]: List of String，修复提交涉及的Tag名；the tag names of this
fixing patch.

 contributors[]: List of JSON, 哪些人/组织帮助确认了这条漏洞报告; used to specify those
persons or organizations who made contributions on this vulnerability record.
 contributors[].org: String, 贡献者组织; the organization info of each contributor


```
 contributors[].name: String, 贡献者名称; the name of each contributor
 contributors[].email: String，贡献者邮箱; the email address of each contributor
 contributors[].contributions: String: 主要贡献内容的描述；the description of the
contributions each contributor has made.
```
 confirm_type : String , 记录当前漏洞报告确认的类型，在人工确认、自动确认或双重
确认当中选择；the confirmation type of this vulnerability record, value should be one of
“manual_confirmed”, “algorithm_confirmed” or “double_confirmed”

 database_specific: JSON object, 各个漏洞库允许在此字段添加附加信息，字段内格式不
受限制，也不影响此漏洞条目本身；holding additional information about the
vulnerability as defined by the database from which the record was obtained. The meaning of
the values within the object is entirely defined by the database and beyond the scope of this
document.

# 4 参考内容

[1] https://www.gov.cn/zhengce/zhengceku/2021-07/14/content_5624965.htm

[2] https://ossf.github.io/OSV-Schema/

[3] https://jsonapi.org/format/

[3] https://cwe.mitre.org/

[4] https://semver.org/

# 5 附录

## 5.1 COSV Schema 1.0制定过程贡献单位及专家名单：

### 1. 初稿拟定：

```
a) 华为云：梁广泰、朱留川
```
### 2. 第一次研讨会：

```
a) 华为云：梁广泰、朱留川、郑志强
```
```
b) 奇安信：董国伟
```

```
c) 中国软件评测中心：袁薇、苏文君柳
```
```
d) 中国科学院软件所：吴敬征
```
```
e) 电子五所：柴思跃
```
```
f) 北京航空航天大学：孙海龙
```
```
g) 军事科学院研究战略咨询评估中心：王鹏
```
```
h) 棱镜七彩：张明辉、但吉兵
```
```
i) 中科院科技战略咨询研究院：隆云滔
```
### 3. 初稿修订：

```
a) 华为云：梁广泰、朱留川、李琳
```
### 4. 第二次评审会：

```
a) 华为云：王千祥、梁广泰、朱留川、李琳、郑志强
```
```
b) 中国软件评测中心：苏文君柳
```
```
c) 棱镜七彩：张明辉、但吉兵
```
```
d) 中科院软件所：王丽敏
```
```
e) 阿里巴巴：郑耿
```
```
f) 奇安信：董国伟
```
### 5. 组织运作：


```
a) 供应链安全工作组秘书：梁广泰
```
### 6. 工作指导：

```
a) 供应链安全工作组领导组：王千祥（组长）、武延军（副组长）、程华（副
组长）
b)
```
## 5.2 附录 – 会议记录：

```
图 1. COSV Schema 第一次线上研讨会
```

```
图 2. COSV Schema 正式评审会现场嘉宾
```
图 3. COSV Schema 正式评审会线上与会嘉宾
