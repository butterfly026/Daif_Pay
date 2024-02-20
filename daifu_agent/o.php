<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8" />
    <title>付款凭证下载</title>
</head>
<body class="">
	<form action="<?php echo $_SERVER['PHP_SELF']; ?>" method="POST" id="">
	<div style="height:200px"></div>
		<div style="text-align:center">
        	<label style="font-size: x-large;">付款订单号</label>
			<input type="text" name="batch_no" id="batch_no" placeholder="TLNC2342211689696830" style="font-size: x-large;" value="<?php echo $batch_no;?>">
		</div>
	<div style="height:50px"></div>
		<div style="text-align:center">
        <button style="font-size: large;" type="submit" id="queryBtn">查询凭证</button>
    	</div>
    </form>
</body>
</html>
	
<?php
function makesign($agent_id, $batch_no, $key, $version=3){
	$str="agent_id=$agent_id&batch_no=$batch_no&key=$key&version=$version";
	$str = strtolower($str);
	$sign = md5($str);
	return strtolower($sign);
}

function curl_get($url){
    $curl = curl_init();
    curl_setopt($curl, CURLOPT_URL, $url);
    curl_setopt($curl, CURLOPT_HEADER, 0);
    curl_setopt($curl, CURLOPT_TIMEOUT, 30);
    curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
    curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, false);
	$proxy = "hk.x4r.cc:1080";
	curl_setopt ($curl, CURLOPT_PROXY, $proxy);
	curl_setopt($curl, CURLOPT_PROXYTYPE, CURLPROXY_SOCKS5);
    $data = curl_exec($curl);
    if (curl_error($curl)) {
        print "Error: " . curl_error($curl);
    } else {
        curl_close($curl);
        return $data;
    }
}

class Crypt3Des {

    public $key = "";

    /* 构造方法 */

    public function encrypt($input) { // 数据加密
        if (empty($input)) {
            return null;
        }
        $size = mcrypt_get_block_size(MCRYPT_3DES, 'ecb');
        $input = $this->pkcs5_pad($input, $size);
        $key = str_pad($this->key, 24, '0');
        $td = mcrypt_module_open(MCRYPT_3DES, '', 'ecb', '');
        $iv = @mcrypt_create_iv(mcrypt_enc_get_iv_size($td), MCRYPT_RAND);
        @mcrypt_generic_init($td, $key, $iv);
        $data = mcrypt_generic($td, $input);
        mcrypt_generic_deinit($td);
        mcrypt_module_close($td);
        return $this->strToHex($data);
    }

    public function decrypt($encrypted) { // 数据解密
        if (!$encrypted || empty($encrypted)) {
            return null;
        }
       $encrypted = $this->hexToStr($encrypted);
        if (!$encrypted || empty($encrypted)) {
            return null;
        }
        $key = str_pad($this->key, 24, '0');
        $td = mcrypt_module_open(MCRYPT_3DES, '', 'ecb', '');
        $iv = @mcrypt_create_iv(mcrypt_enc_get_iv_size($td), MCRYPT_RAND);
        $ks = mcrypt_enc_get_key_size($td);
        @mcrypt_generic_init($td, $key, $iv);
        $decrypted = mdecrypt_generic($td, $encrypted);
        mcrypt_generic_deinit($td);
        mcrypt_module_close($td);
        $y = $this->pkcs5_unpad($decrypted);
        return $y;
    }

    function pkcs5_pad($text, $blocksize) {
        $pad = $blocksize - (strlen($text) % $blocksize);
        return $text . str_repeat(chr($pad), $pad);
    }

    function pkcs5_unpad($text) {
        $pad = ord($text {strlen($text) - 1});
        if ($pad > strlen($text)) {
            return false;
        }
        if (strspn($text, chr($pad), strlen($text) - $pad) != $pad) {
            return false;
        }
        return substr($text, 0, - 1 * $pad);
    }

    function PaddingPKCS7($data) {
        $block_size = mcrypt_get_block_size(MCRYPT_3DES, MCRYPT_MODE_CBC);
        $padding_char = $block_size - (strlen($data) % $block_size);
        $data .= str_repeat(chr($padding_char), $padding_char);
        return $data;
    }

    function strToHex($string) {
        $hex = "";
        for ($i = 0; $i < strlen($string); $i++) {
            $iHex = dechex(ord($string[$i]));
            if (strlen($iHex) == 1)
                $hex .= '0' . $iHex;
            else
                $hex .= $iHex;
        }
        $hex = strtoupper($hex);
        return $hex;
    }

    function hexToStr($hex) {
        $string = "";
        for ($i = 0; $i < strlen($hex) - 1; $i += 2) {
            $string .= chr(hexdec($hex[$i] . $hex[$i + 1]));
        }
        return $string;
    }

}



$dfdes='30B99AC42B174D25B7CC45C5';//des密钥
$dfmd5='3F8D44E2AAAD43EAB6B3DD02'; //MD5密钥1
$qmd5='250F6E5C9E55477793D78DBD'; //MD5密钥2
$mer='2154718'; //商户号,agent_id

$base_url = "https://www.heepay.com/API/PayTransit/PayTransferGetProof.aspx";



if (isset($_POST["batch_no"])){
	$batch_no = $_POST["batch_no"];
} else {
	$batch_no = null;
}


if ($batch_no != null){
	$sign = makesign($mer, $batch_no, $dfmd5, 3);	
	$url= $base_url . "?agent_id=$mer&batch_no=$batch_no&version=3&sign=$sign";
	$ret = curl_get($url);
	$strUTF8 = mb_convert_encoding($ret, 'utf-8', 'gb2312');
	$simpleXMLElement = simplexml_load_string($strUTF8);
	$ret1 = (string)$simpleXMLElement->ret_code;
	if ($ret1 != "0000"){
		echo "<div style='height:50px'></div><div style='text-align:center'>接口返回错误$ret1</div>";
		exit();
	}
	$input = (string)$simpleXMLElement->file_path;

	
	//$input = substr($strUTF8, 139, strlen($strUTF8)-203);
	//$input = "F0F7679F2E0E25B30990017BEF4268D7746379A195F37C6FA8C3FA6B6B2AA9E765E03D89AEB6839AD1F20B885524C5A012AB75E19424EFBEC8F6311676F140F9F692A08FCDCC7A95E30F14D17BA3536A27FB68EA6C2FF901091593AD7D228335EC6E68737238F339";
	
	$rep = new Crypt3Des (); // 初始化一个对象
	$rep->key = $dfdes;
    $pdf_link = $rep->decrypt($input);
	$contents = file_get_contents($pdf_link);
	if ($contents!= false){
		if (!file_exists('./savepdf')){
			mkdir('./savepdf', 0777, true);
		}
		$mypdfpath = "./savepdf/$batch_no.pdf";
		file_put_contents($mypdfpath, $contents);
		echo "<div style='height:50px'></div><div style='text-align:center'><a href=$mypdfpath>查看支付凭证</a></div>";
	}else{
		echo "<div style='height:50px'></div><div style='text-align:center'>凭证URL错误</div>";
	}
}
?>
