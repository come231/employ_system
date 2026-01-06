package com.hkd.util;

/**
 * 拼音工具类 - 将中文姓名转换为拼音
 * 简化版本，使用预定义的常用汉字拼音映射
 */
public class PinyinUtil {

    // 常用姓氏拼音映射
    private static final String[][] SURNAME_MAP = {
        {"赵", "zhao"}, {"钱", "qian"}, {"孙", "sun"}, {"李", "li"},
        {"周", "zhou"}, {"吴", "wu"}, {"郑", "zheng"}, {"王", "wang"},
        {"冯", "feng"}, {"陈", "chen"}, {"褚", "chu"}, {"卫", "wei"},
        {"蒋", "jiang"}, {"沈", "shen"}, {"韩", "han"}, {"杨", "yang"},
        {"朱", "zhu"}, {"秦", "qin"}, {"尤", "you"}, {"许", "xu"},
        {"何", "he"}, {"吕", "lv"}, {"施", "shi"}, {"张", "zhang"},
        {"孔", "kong"}, {"曹", "cao"}, {"严", "yan"}, {"华", "hua"},
        {"金", "jin"}, {"魏", "wei"}, {"陶", "tao"}, {"姜", "jiang"},
        {"戚", "qi"}, {"谢", "xie"}, {"邹", "zou"}, {"喻", "yu"},
        {"柏", "bai"}, {"水", "shui"}, {"窦", "dou"}, {"章", "zhang"},
        {"云", "yun"}, {"苏", "su"}, {"潘", "pan"}, {"葛", "ge"},
        {"奚", "xi"}, {"范", "fan"}, {"彭", "peng"}, {"郎", "lang"},
        {"鲁", "lu"}, {"韦", "wei"}, {"昌", "chang"}, {"马", "ma"},
        {"苗", "miao"}, {"凤", "feng"}, {"花", "hua"}, {"方", "fang"},
        {"俞", "yu"}, {"任", "ren"}, {"袁", "yuan"}, {"柳", "liu"},
        {"酆", "feng"}, {"鲍", "bao"}, {"史", "shi"}, {"唐", "tang"},
        {"费", "fei"}, {"廉", "lian"}, {"岑", "cen"}, {"薛", "xue"},
        {"雷", "lei"}, {"贺", "he"}, {"倪", "ni"}, {"汤", "tang"},
        {"滕", "teng"}, {"殷", "yin"}, {"罗", "luo"}, {"毕", "bi"},
        {"郝", "hao"}, {"邬", "wu"}, {"安", "an"}, {"常", "chang"},
        {"乐", "le"}, {"于", "yu"}, {"时", "shi"}, {"傅", "fu"},
        {"皮", "pi"}, {"卞", "bian"}, {"齐", "qi"}, {"康", "kang"},
        {"伍", "wu"}, {"余", "yu"}, {"元", "yuan"}, {"卜", "bu"},
        {"顾", "gu"}, {"孟", "meng"}, {"平", "ping"}, {"黄", "huang"},
        {"和", "he"}, {"穆", "mu"}, {"萧", "xiao"}, {"尹", "yin"},
        {"姚", "yao"}, {"邵", "shao"}, {"湛", "zhan"}, {"汪", "wang"},
        {"祁", "qi"}, {"毛", "mao"}, {"禹", "yu"}, {"狄", "di"},
        {"米", "mi"}, {"贝", "bei"}, {"明", "ming"}, {"臧", "zang"},
        {"计", "ji"}, {"伏", "fu"}, {"成", "cheng"}, {"戴", "dai"},
        {"谈", "tan"}, {"宋", "song"}, {"茅", "mao"}, {"庞", "pang"},
        {"熊", "xiong"}, {"纪", "ji"}, {"舒", "shu"}, {"屈", "qu"},
        {"项", "xiang"}, {"祝", "zhu"}, {"董", "dong"}, {"梁", "liang"},
        {"杜", "du"}, {"阮", "ruan"}, {"蓝", "lan"}, {"闵", "min"},
        {"席", "xi"}, {"季", "ji"}, {"麻", "ma"}, {"强", "qiang"},
        {"贾", "jia"}, {"路", "lu"}, {"娄", "lou"}, {"危", "wei"},
        {"江", "jiang"}, {"童", "tong"}, {"颜", "yan"}, {"郭", "guo"},
        {"梅", "mei"}, {"盛", "sheng"}, {"林", "lin"}, {"刁", "diao"},
        {"钟", "zhong"}, {"徐", "xu"}, {"邱", "qiu"}, {"骆", "luo"},
        {"高", "gao"}, {"夏", "xia"}, {"蔡", "cai"}, {"田", "tian"},
        {"樊", "fan"}, {"胡", "hu"}, {"凌", "ling"}, {"霍", "huo"},
        {"虞", "yu"}, {"万", "wan"}, {"支", "zhi"}, {"柯", "ke"},
        {"昝", "zan"}, {"管", "guan"}, {"卢", "lu"}, {"莫", "mo"},
        {"经", "jing"}, {"房", "fang"}, {"裘", "qiu"}, {"缪", "miao"},
        {"干", "gan"}, {"解", "xie"}, {"应", "ying"}, {"宗", "zong"},
        {"丁", "ding"}, {"宣", "xuan"}, {"贲", "ben"}, {"邓", "deng"},
        {"郁", "yu"}, {"单", "shan"}, {"杭", "hang"}, {"洪", "hong"},
        {"包", "bao"}, {"诸", "zhu"}, {"左", "zuo"}, {"石", "shi"},
        {"崔", "cui"}, {"吉", "ji"}, {"钮", "niu"}, {"龚", "gong"},
        {"程", "cheng"}, {"嵇", "ji"}, {"邢", "xing"}, {"滑", "hua"},
        {"裴", "pei"}, {"陆", "lu"}, {"荣", "rong"}, {"翁", "weng"},
        {"荀", "xun"}, {"羊", "yang"}, {"惠", "hui"}, {"甄", "zhen"},
        {"曲", "qu"}, {"家", "jia"}, {"封", "feng"}, {"芮", "rui"},
        {"羿", "yi"}, {"储", "chu"}, {"靳", "jin"}, {"汲", "ji"},
        {"邴", "bing"}, {"糜", "mi"}, {"松", "song"}, {"井", "jing"},
        {"段", "duan"}, {"富", "fu"}, {"巫", "wu"}, {"乌", "wu"},
        {"焦", "jiao"}, {"巴", "ba"}, {"弓", "gong"}, {"牧", "mu"},
        {"隗", "wei"}, {"山", "shan"}, {"谷", "gu"}, {"车", "che"},
        {"侯", "hou"}, {"宓", "mi"}, {"蓬", "peng"}, {"全", "quan"},
        {"郗", "xi"}, {"班", "ban"}, {"仰", "yang"}, {"秋", "qiu"},
        {"仲", "zhong"}, {"伊", "yi"}, {"宫", "gong"}, {"宁", "ning"},
        {"仇", "qiu"}, {"栾", "luan"}, {"暴", "bao"}, {"甘", "gan"},
        {"钭", "tou"}, {"厉", "li"}, {"戎", "rong"}, {"祖", "zu"},
        {"武", "wu"}, {"符", "fu"}, {"刘", "liu"}, {"景", "jing"},
        {"詹", "zhan"}, {"束", "shu"}, {"龙", "long"}, {"叶", "ye"},
        {"幸", "xing"}, {"司", "si"}, {"韶", "shao"}, {"郜", "gao"},
        {"黎", "li"}, {"蓟", "ji"}, {"薄", "bo"}, {"印", "yin"},
        {"宿", "su"}, {"白", "bai"}, {"怀", "huai"}, {"蒲", "pu"},
        {"邰", "tai"}, {"从", "cong"}, {"鄂", "e"}, {"索", "suo"},
        {"咸", "xian"}, {"籍", "ji"}, {"赖", "lai"}, {"卓", "zhuo"},
        {"蔺", "lin"}, {"屠", "tu"}, {"蒙", "meng"}, {"池", "chi"},
        {"乔", "qiao"}, {"阴", "yin"}, {"郁", "yu"}, {"胥", "xu"},
        {"能", "neng"}, {"苍", "cang"}, {"双", "shuang"}, {"闻", "wen"},
        {"莘", "shen"}, {"党", "dang"}, {"翟", "zhai"}, {"谭", "tan"},
        {"贡", "gong"}, {"劳", "lao"}, {"逄", "pang"}, {"姬", "ji"},
        {"申", "shen"}, {"扶", "fu"}, {"堵", "du"}, {"冉", "ran"},
        {"宰", "zai"}, {"郦", "li"}, {"雍", "yong"}, {"郤", "xi"},
        {"璩", "qu"}, {"桑", "sang"}, {"桂", "gui"}, {"濮", "pu"},
        {"牛", "niu"}, {"寿", "shou"}, {"通", "tong"}, {"边", "bian"},
        {"扈", "hu"}, {"燕", "yan"}, {"冀", "ji"}, {"郏", "jia"},
        {"浦", "pu"}, {"尚", "shang"}, {"农", "nong"}, {"温", "wen"},
        {"别", "bie"}, {"庄", "zhuang"}, {"晏", "yan"}, {"柴", "chai"},
        {"瞿", "qu"}, {"阎", "yan"}, {"充", "chong"}, {"慕", "mu"},
        {"连", "lian"}, {"茹", "ru"}, {"习", "xi"}, {"宦", "huan"},
        {"艾", "ai"}, {"鱼", "yu"}, {"容", "rong"}, {"向", "xiang"},
        {"古", "gu"}, {"易", "yi"}, {"慎", "shen"}, {"戈", "ge"},
        {"廖", "liao"}, {"庾", "yu"}, {"终", "zhong"}, {"暨", "ji"},
        {"居", "ju"}, {"衡", "heng"}, {"步", "bu"}, {"都", "du"},
        {"耿", "geng"}, {"满", "man"}, {"弘", "hong"}, {"匡", "kuang"},
        {"国", "guo"}, {"文", "wen"}, {"寇", "kou"}, {"广", "guang"},
        {"禄", "lu"}, {"阙", "que"}, {"东", "dong"}, {"欧", "ou"},
        {"殳", "shu"}, {"沃", "wo"}, {"利", "li"}, {"蔚", "wei"},
        {"越", "yue"}, {"夔", "kui"}, {"隆", "long"}, {"师", "shi"},
        {"巩", "gong"}, {"厍", "she"}, {"聂", "nie"}, {"晁", "chao"},
        {"勾", "gou"}, {"敖", "ao"}, {"融", "rong"}, {"冷", "leng"},
        {"訾", "zi"}, {"辛", "xin"}, {"阚", "kan"}, {"那", "na"},
        {"简", "jian"}, {"饶", "rao"}, {"空", "kong"}, {"曾", "zeng"},
        {"毋", "wu"}, {"沙", "sha"}, {"乜", "nie"}, {"养", "yang"},
        {"鞠", "ju"}, {"须", "xu"}, {"丰", "feng"}, {"巢", "chao"},
        {"关", "guan"}, {"蒯", "kuai"}, {"相", "xiang"}, {"查", "zha"},
        {"后", "hou"}, {"荆", "jing"}, {"红", "hong"}, {"游", "you"},
        {"竺", "zhu"}, {"权", "quan"}, {"逯", "lu"}, {"盖", "gai"},
        {"益", "yi"}, {"桓", "huan"}, {"公", "gong"}, {"仉", "zhang"},
        {"督", "du"}, {"晋", "jin"}, {"楚", "chu"}, {"阎", "yan"},
        {"法", "fa"}, {"汝", "ru"}, {"鄢", "yan"}, {"涂", "tu"},
        {"钦", "qin"}, {"归", "gui"}, {"海", "hai"}, {"岳", "yue"},
        {"帅", "shuai"}, {"缑", "gou"}, {"亢", "kang"}, {"况", "kuang"},
        {"后", "hou"}, {"有", "you"}, {"琴", "qin"}, {"商", "shang"},
        {"牟", "mou"}, {"佘", "she"}, {"佴", "nai"}, {"伯", "bo"},
        {"赏", "shang"}, {"墨", "mo"}, {"哈", "ha"}, {"谯", "qiao"},
        {"笪", "da"}, {"年", "nian"}, {"爱", "ai"}, {"阳", "yang"},
        {"佟", "tong"}, {"言", "yan"}, {"福", "fu"}, {"百", "bai"},
        {"家", "jia"}, {"姓", "xing"}, {"续", "xu"}
    };

    // 常用汉字拼音映射（名字常用字）
    private static final String[][] COMMON_CHAR_MAP = {
        {"明", "ming"}, {"强", "qiang"}, {"伟", "wei"}, {"芳", "fang"},
        {"娜", "na"}, {"敏", "min"}, {"静", "jing"}, {"丽", "li"},
        {"涛", "tao"}, {"洋", "yang"}, {"勇", "yong"}, {"军", "jun"},
        {"杰", "jie"}, {"磊", "lei"}, {"鹏", "peng"}, {"飞", "fei"},
        {"超", "chao"}, {"婷", "ting"}, {"雪", "xue"}, {"燕", "yan"},
        {"萍", "ping"}, {"艳", "yan"}, {"玲", "ling"}, {"霞", "xia"},
        {"秀", "xiu"}, {"英", "ying"}, {"华", "hua"}, {"慧", "hui"},
        {"巧", "qiao"}, {"美", "mei"}, {"娟", "juan"}, {"凤", "feng"},
        {"春", "chun"}, {"红", "hong"}, {"兰", "lan"}, {"莉", "li"},
        {"梅", "mei"}, {"桂", "gui"}, {"芬", "fen"}, {"青", "qing"},
        {"珍", "zhen"}, {"琳", "lin"}, {"君", "jun"}, {"云", "yun"},
        {"健", "jian"}, {"浩", "hao"}, {"晓", "xiao"}, {"宇", "yu"},
        {"辉", "hui"}, {"刚", "gang"}, {"平", "ping"}, {"建", "jian"},
        {"国", "guo"}, {"文", "wen"}, {"志", "zhi"}, {"成", "cheng"},
        {"东", "dong"}, {"海", "hai"}, {"波", "bo"}, {"旭", "xu"},
        {"阳", "yang"}, {"天", "tian"}, {"龙", "long"}, {"虎", "hu"},
        {"彪", "biao"}, {"斌", "bin"}, {"亮", "liang"}, {"新", "xin"},
        {"峰", "feng"}, {"松", "song"}, {"林", "lin"}, {"森", "sen"},
        {"鑫", "xin"}, {"淼", "miao"}, {"焱", "yan"}, {"垚", "yao"},
        {"宏", "hong"}, {"毅", "yi"}, {"俊", "jun"}, {"哲", "zhe"},
        {"博", "bo"}, {"睿", "rui"}, {"远", "yuan"}, {"翔", "xiang"},
        {"腾", "teng"}, {"达", "da"}, {"川", "chuan"}, {"正", "zheng"},
        {"一", "yi"}, {"二", "er"}, {"三", "san"}, {"四", "si"},
        {"五", "wu"}, {"六", "liu"}, {"七", "qi"}, {"八", "ba"},
        {"九", "jiu"}, {"十", "shi"}, {"百", "bai"}, {"千", "qian"},
        {"万", "wan"}, {"亿", "yi"}, {"兆", "zhao"}, {"京", "jing"},
        {"大", "da"}, {"中", "zhong"}, {"小", "xiao"}, {"少", "shao"},
        {"老", "lao"}, {"长", "chang"}, {"永", "yong"}, {"安", "an"},
        {"全", "quan"}, {"保", "bao"}, {"卫", "wei"}, {"守", "shou"},
        {"民", "min"}, {"贵", "gui"}, {"富", "fu"}, {"财", "cai"},
        {"禄", "lu"}, {"寿", "shou"}, {"康", "kang"}, {"宁", "ning"},
        {"庆", "qing"}, {"德", "de"}, {"福", "fu"}, {"善", "shan"},
        {"义", "yi"}, {"礼", "li"}, {"智", "zhi"}, {"信", "xin"},
        {"忠", "zhong"}, {"孝", "xiao"}, {"仁", "ren"}, {"爱", "ai"},
        {"诚", "cheng"}, {"勤", "qin"}, {"俭", "jian"}, {"廉", "lian"},
        {"洁", "jie"}, {"恒", "heng"}, {"根", "gen"}, {"生", "sheng"},
        {"星", "xing"}, {"月", "yue"}, {"日", "ri"}, {"光", "guang"},
        {"明", "ming"}, {"朝", "chao"}, {"晨", "chen"}, {"露", "lu"},
        {"雨", "yu"}, {"霖", "lin"}, {"雯", "wen"}, {"霆", "ting"},
        {"电", "dian"}, {"雷", "lei"}, {"风", "feng"}, {"云", "yun"},
        {"冰", "bing"}, {"雪", "xue"}, {"霜", "shuang"}, {"寒", "han"},
        {"晴", "qing"}, {"暖", "nuan"}, {"热", "re"}, {"温", "wen"},
        {"柔", "rou"}, {"刚", "gang"}, {"硬", "ying"}, {"坚", "jian"},
        {"韧", "ren"}, {"实", "shi"}, {"虚", "xu"}, {"空", "kong"},
        {"满", "man"}, {"盈", "ying"}, {"丰", "feng"}, {"茂", "mao"},
        {"荣", "rong"}, {"华", "hua"}, {"贵", "gui"}, {"尊", "zun"},
        {"高", "gao"}, {"雅", "ya"}, {"正", "zheng"}, {"直", "zhi"},
        {"真", "zhen"}, {"纯", "chun"}, {"净", "jing"}, {"清", "qing"},
        {"澈", "che"}, {"透", "tou"}, {"亮", "liang"}, {"晶", "jing"},
        {"莹", "ying"}, {"瑶", "yao"}, {"琪", "qi"}, {"珊", "shan"},
        {"珠", "zhu"}, {"宝", "bao"}, {"玉", "yu"}, {"翠", "cui"},
        {"翡", "fei"}, {"碧", "bi"}, {"蓝", "lan"}, {"紫", "zi"},
        {"红", "hong"}, {"橙", "cheng"}, {"黄", "huang"}, {"绿", "lv"},
        {"青", "qing"}, {"蓝", "lan"}, {"紫", "zi"}, {"白", "bai"},
        {"黑", "hei"}, {"灰", "hui"}, {"金", "jin"}, {"银", "yin"},
        {"铜", "tong"}, {"铁", "tie"}, {"钢", "gang"}, {"锡", "xi"},
        {"锌", "xin"}, {"铅", "qian"}, {"锂", "li"}, {"钠", "na"},
        {"镁", "mei"}, {"钙", "gai"}, {"钾", "jia"}, {"钡", "bei"},
        {"子", "zi"}, {"女", "nv"}, {"男", "nan"}, {"父", "fu"},
        {"母", "mu"}, {"兄", "xiong"}, {"弟", "di"}, {"姐", "jie"},
        {"妹", "mei"}, {"叔", "shu"}, {"婶", "shen"}, {"姑", "gu"},
        {"舅", "jiu"}, {"爷", "ye"}, {"奶", "nai"}, {"外", "wai"},
        {"祖", "zu"}, {"宗", "zong"}, {"家", "jia"}, {"族", "zu"},
        {"嘉", "jia"}, {"佳", "jia"}, {"凯", "kai"}, {"楷", "kai"},
        {"锐", "rui"}, {"瑞", "rui"}, {"睿", "rui"}, {"思", "si"},
        {"诗", "shi"}, {"琦", "qi"}, {"祺", "qi"}, {"奇", "qi"},
        {"乐", "le"}, {"悦", "yue"}, {"欣", "xin"}, {"欢", "huan"},
        {"喜", "xi"}, {"庆", "qing"}, {"祥", "xiang"}, {"祯", "zhen"},
        {"祐", "you"}, {"佑", "you"}, {"辰", "chen"}, {"晨", "chen"},
        {"宸", "chen"}, {"逸", "yi"}, {"轩", "xuan"}, {"萱", "xuan"},
        {"瑄", "xuan"}, {"渊", "yuan"}, {"源", "yuan"}, {"圆", "yuan"}
    };

    /**
     * 将中文姓名转换为拼音
     * @param chineseName 中文姓名
     * @return 拼音（小写，无空格）
     */
    public static String toPinyin(String chineseName) {
        if (chineseName == null || chineseName.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < chineseName.length(); i++) {
            String character = String.valueOf(chineseName.charAt(i));
            String pinyin = findPinyin(character);
            result.append(pinyin);
        }

        return result.toString().toLowerCase();
    }

    /**
     * 查找单个汉字的拼音
     * @param character 单个汉字
     * @return 拼音
     */
    private static String findPinyin(String character) {
        // 先在姓氏映射中查找
        for (String[] mapping : SURNAME_MAP) {
            if (mapping[0].equals(character)) {
                return mapping[1];
            }
        }

        // 再在常用字映射中查找
        for (String[] mapping : COMMON_CHAR_MAP) {
            if (mapping[0].equals(character)) {
                return mapping[1];
            }
        }

        // 如果是非中文字符，直接返回
        if (character.matches("[a-zA-Z0-9]")) {
            return character.toLowerCase();
        }

        // 未找到则返回空字符串或使用Unicode编码
        return "x";
    }

    /**
     * 生成唯一的用户名
     * 如果用户名已存在，则在后面加数字
     * @param basePinyin 基础拼音
     * @param existingCheck 检查用户名是否存在的函数
     * @return 唯一的用户名
     */
    public static String generateUniqueUsername(String basePinyin, java.util.function.Function<String, Boolean> existingCheck) {
        if (basePinyin == null || basePinyin.isEmpty()) {
            basePinyin = "user";
        }

        String username = basePinyin;
        int suffix = 1;

        while (existingCheck.apply(username)) {
            username = basePinyin + suffix;
            suffix++;
        }

        return username;
    }

    public static void main(String[] args) {
        // 测试
        System.out.println(toPinyin("张三")); // zhangsan
        System.out.println(toPinyin("李明")); // liming
        System.out.println(toPinyin("王小红")); // wangxiaohong
    }
}
