<?php

/*
LOGIC

search by 'link' (link on the page)

if it does not exist and is a page: insert with 'unknown' for file
if it does not exist and is a file: insert with same string for link and file

if it does exist and file is not blank: look on 'link' for file with the same extension and update file and ts
if it does exist and file is blank or unknown: update ts and write 'unknown' for file if not already




EXISTENCE
-does exist
-does not exist

FILE
-blank
-contain something

LINK
-can't be blank if it exists

TS
-can be recent
-can be old

PAGE
-can't be blank - title of page containing the link

NAME - clickable text in the link




*/

include 'functions.php';



function rel2abs($rel, $base)
{
    /* return if already absolute URL */
    if (parse_url($rel, PHP_URL_SCHEME) != '') return $rel;

    /* queries and anchors */
    if ($rel[0]=='#' || $rel[0]=='?') return $base.$rel;

    /* parse base URL and convert to local variables:
       $scheme, $host, $path */
    extract(parse_url($base));

    /* remove non-directory element from path */
    $path = preg_replace('#/[^/]*$#', '', $path);

    /* destroy path if relative url points to root */
    if ($rel[0] == '/') $path = '';

    /* dirty absolute URL */
    $abs = "$host$path/$rel";

    /* replace '//' or '/./' or '/foo/../' with '/' */
    $re = array('#(/\.?/)#', '#/(?!\.\.)[^/]+/\.\./#');
    for($n=1; $n>0; $abs=preg_replace($re, '/', $abs, -1, $n)) {}

    /* absolute URL is ready! */
    return $scheme.'://'.$abs;
}



function isURLFile($url)
{
	$url= rtrim($url,"/");
	$sectionsBySlash = explode("/", $url);
	$last = end($sectionsBySlash);
	$sectionsByPeriod = explode(".", $last);
	$last = end($sectionsByPeriod);
	if ($last == 'zip' || $last == '7z' || $last == 'bin' || $last == 'cia' || $last == 'exe' || $last = 'nds' || $last == '3dsx') 
	{
    		return true;
	}
	return false;
}

function findFile($url, $ext)
{
	$html = file_get_contents($url);
	$dom = new DOMDocument;
	libxml_use_internal_errors(true);
	$dom->loadHtml($html);
	libxml_use_internal_errors(false);
	
	$arr = $dom->getElementsByTagName("a");
	foreach($arr as $item)
	{
		
		$href = $item->getAttribute("href");	
		$bits = explode(".", $href);
		$last = end($bits);
		if($last == $ext)
		{
			return $href;
		}
	}
	return "";
}


function write($url, &$urlsconverted, &$Db)
{
        $actuallink=$url;
	if($actuallink[0] != 'h')
	{
		$actuallink='https://quantumcat1.github.io/'.$actuallink;
	}
        $actuallink = rtrim($actuallink,"/");
        //echo $actuallink.' '; 

        $html = file_get_contents($actuallink);
	$dom = new DOMDocument;
	libxml_use_internal_errors(true);
	$dom->loadHtml($html);
	libxml_use_internal_errors(false);
	$query = "";
	$now = date("Y-m-d H:i:s");
	
	//get title of the page of the guide we're on
	
	$list = $dom->getElementsByTagName("h1");
	foreach($list as $thing) $title = trim($thing->nodeValue);

        $container = $dom->getElementById("files");
        if(is_object($container))
        {
            $arr = $container->getElementsByTagName("a");
            
            foreach($arr as $item) //for each link on the guide page
            {
                  $text = trim(preg_replace("/[\r\n]+/", " ", $item->nodeValue));
                  $href =  $item->getAttribute("href"); //to the page where files are, not necessarily the file itself
                  $query = "SELECT * FROM 3DSLinks.File WHERE link = '$href' AND page = '$title'";
                  
                  $files = array();
                  $files = $Db->select($query); //find out if we have this link already
                  //echo "<p>.query is $query";
                  if(sizeof($files) > 0) //if the link already exists in the database
                  {
                  	$filelink = trim($files[0]['file']);
                  	$linklink = trim($files[0]['link']);
                  	//file blank or unknown - just update timestamp
                  	if($filelink == '' || $filelink == 'unknown' || strpos($linklink, 'magnet') !== false || $linklink == $filelink)
                  	{
                  		if($filelink == '') $filelink = 'unknown';
                  		$query = "UPDATE 3DSLinks.File SET ts = '$now', name = '$text', file = '$filelink' WHERE link = '$linklink' and page = '$title'";
                  	} 
                  	else
                  	{
                  		//look for the first file with the same extension (in case of update)
                  		//get extension
                  		$sectionsByPeriod = explode(".", $filelink);
				$last = end($sectionsByPeriod);
				if(strpos($linklink, "http") === FALSE)
				{
					$linklink = ltrim($linklink, "/"); //in case it starts with a /
					$linklink = 'https://3ds.guide/'.$linklink;
				}
				$newfilelink = "";
				if(trim($last) != "" && substr($linklink, 0, 10) == substr($filelink, 0, 10)) 
				{
					$newfilelink = findFile($linklink, $last);
				}
				if($newfilelink != "") $newfilelink = rel2abs($newfilelink, $linklink);
				
				if(trim($newfilelink) != '')//if we find a file with the same extension - this should always happen
				{
					$query = "UPDATE 3DSLinks.File SET file = '$newfilelink', name = '$text', ts = '$now' WHERE link = '$linklink' and page = '$page'";
				}
				else //assuming we always find a file with the same extension the below should not happen (except if the direct file link was entered manually and is a different domain to the link)
				{
					$query = "UPDATE 3DSLinks.File SET ts = '$now', name = '$text' WHERE link = '$linklink' and page = '$page'";
				}
                  	}
                  	
                  }
                  else //new link that isn't in the database yet
                  {
                  	if(strpos($href, "http") === FALSE)
			{
				$href = ltrim($href, "/"); //in case it starts with a /
				$href = 'https://3ds.guide/'.$href;
			}
			$linkisfile = isURLFile($href);
                  	//link is a direct file
                  	if($linkisfile)
                  	{
                  		$query = "INSERT INTO 3DSLinks.File (page, name, link, file, path, ts) VALUES ('$title', '$text', '$href', '$href', './', '$now')";
                  	}
                  	//not a file but is a magnet link
                  	elseif(strpos($href, 'magnet') !== FALSE)
                  	{
                  		$query = "INSERT INTO 3DSLinks.File (page, name, link, file, path, ts) VALUES ('$title', '$text', '$href', 'magnet', './', '$now')";
                  	}
                  	//link is a page
                  	else
                  	{
                  		$query = "INSERT INTO 3DSLinks.File (page, name, link, file, path, ts) VALUES ('$title', '$text', '$href', 'unknown', './', '$now')";
                  	}
                  }

                  $result = $Db->query($query);
                  //echo "<p>query is $query";
                  /*if(!$result) 
                  {
                  	echo "<p>query $query failed", PHP_EOL;
                  }
                  else
                  {
                  	echo "<p>query $query succeeded", PHP_EOL;
                  }*/
            }
        }
        //echo "<p>pushing $url into the array";
        array_push($urlsconverted, $url);
}

function findLinks($url, &$urlsconverted, &$Db)
{
	$actuallink=$url;
	if($actuallink[0] != 'h')
	{
		$actuallink='https://quantumcat1.github.io/'.$actuallink;
	}
	$html = file_get_contents($actuallink);
	$dom = new DOMDocument;
	libxml_use_internal_errors(true);
	$dom->loadHtml($html);
	libxml_use_internal_errors(false);

	$links = $dom->getElementsByTagName('a');
	$links_to_return = array();

	foreach ($links as $link)
	{		
		$linkstring = $link->getAttribute('href');
		if($linkstring[0] != 'h')
		{
			$linkstring = 'https://quantumcat1.github.io/'.$actuallink;
		}
	        $linkstring = rtrim($linkstring,"/");
		$inguide = (strpos($linkstring, 'quantumcat1.github.io') !== FALSE);
		$alreadyconverted = in_array($linkstring, $urlsconverted);
		
		if($inguide && !$alreadyconverted)
		{
			$alink = $link->getAttribute('href');
			$links_to_return[$alink] = $alink;
		}
	}
	//print_r($links_to_return);
	return $links_to_return;
}

function recursiveConvert($baseurl, &$urlsconverted, &$Db)
{
	if(!in_array($baseurl, $urlsconverted))
	{
		write($baseurl, $urlsconverted, $Db);
		$newlinks = array();
		$newlinks = findLinks($baseurl, $urlsconverted, $Db);
		
		if($newlinks != null)
		{
			foreach($newlinks as $link)
			{
				recursiveConvert($link, $urlsconverted, $Db);
			}
		}
	}
}
ini_set('display_errors', 1);

global $urlsconverted;
$Db = new Db("3DSLinks");
$urlsconverted = array();
//recursiveConvert('https://quantumcat1.github.io/', $urlsconverted, $Db);
recursiveConvert('https://quantumcat1.github.io/site-navigation', $urlsconverted, $Db);


?>