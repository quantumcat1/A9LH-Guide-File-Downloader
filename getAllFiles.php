<?php

include 'functions.php';

class Page
{
    public $title;
    public $files = array();
}

class F
{
    public $link;
    public $file;
    public $page;
    public $name;
    public $path;
    public $region;
    public $firmware;
    public $type;
    public $ts;
}

$Db = new Db("3DSLinks");

$query = "SELECT * FROM 3DSLinks.File ORDER BY page";

$all = $Db->select($query);

$array = array();
$page = new Page();
$page->title = $all[0]['page'];
foreach($all as $row)
{
    if($row['page'] == $page->title)
    {
        //$page->files[] = $row['file'];
        $f = new F();
        $f->file = $row['file'];
        $f->link = $row['link'];
        $f->name = $row['name'];
        $f->ts = $row['ts'];
        $f->page = $row['page'];
        $f->path = $row['path'];
        $f->region = $row['region'];
        $f->firmware = $row['firmware'];
        $f->type = $row['type'];
        $page->files[] = $f;
    }
    else
    {
        $array[] = $page;
        $page = new Page();
        $page->title = $row['page'];
        //$page->files[] = $row['file'];
        $f = new F();
        $f->file = $row['file'];
        $f->link = $row['link'];
        $f->name = $row['name'];
        $f->ts = $row['ts'];
        $f->page = $row['page'];
        $f->path = $row['path'];
        $f->region = $row['region'];
        $f->firmware = $row['firmware'];
        $f->type = $row['type'];
        $page->files[] = $f;
    }
}

$array[] = $page; //to get the last one

echo json_encode($array);











?>