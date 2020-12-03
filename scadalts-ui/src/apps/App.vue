<template>
    <div id="app">
        <input type="checkbox" id="check-sidebar" class="nav-sidebar">
        <label for="check-sidebar" class="nav-sidebar">
            <i id="btn-sidebar"><font-awesome-icon icon="bars" /></i>
            <i id="cancel-sidebar"><font-awesome-icon icon="times" /></i>
        </label>
        <div class="sidebar">
            <logo>Scada-LTS</logo>
            <a href="#/alarms" v-bind:class="{ active: $route.name=='alarms'}"><i><font-awesome-icon icon="bell" /></i><span>Alarms</span></a>
            <a href="#/historical-alarms" v-bind:class="{ active: $route.name=='historical-alarms'}"><i><font-awesome-icon icon="file-medical-alt"/></i><span>Historical Alarms</span></a>
            <a href="#/notification-list" v-bind:class="{ active: $route.name=='notification-list'}"><i><font-awesome-icon icon="file-medical-alt"/></i><span>Notification List</span></a>
            <a href="#/about" v-bind:class="{ active: $route.name=='about'}"><i><font-awesome-icon icon="info"/></i><span>About</span></a>
            <a href="./watch_list.shtm" v-bind:class="{ active: $route.name=='watch_list'}"><i><font-awesome-icon icon="list-alt"/></i><span>Watch list</span></a>
        </div>
        <header>
            <navnui>
                <ul class="navnui-left" id="navigation-left">
                    <li><a href="/ScadaBR/watch_list.shtm">Watch List</a></li>
                </ul>
                <ul class="navnui-right">
                    <li><a href="#">My Acount</a></li>
                    <li style="font-size: 5px">v0.0.1.1</li>
                </ul>

            </navnui>
        </header>
        <div class="view">
            <router-view></router-view>
        </div>
    </div>

</template>

<script>

    export default {
        name: 'app',
        components: {
        }
    }
</script>

<style scoped>
   /* @import '../../node_modules/ag-grid-community/dist/styles/ag-grid.css';
    @import '../../node_modules/ag-grid-community/dist/styles/ag-theme-balham.css';*/


   :root {
       font-size: 16px;
       font-family: OpenSans-Regular;
   }
   * {
       margin: 0;
       padding: 0;
       text-decoration: none;
   }
   .sidebar{
       z-index: 10;
       position: fixed;
       margin-top: -20px;
       left: -240px;
       width: 240px;
       height: 100%;
       background-color: #141418;
       transition: all .5s ease;
   }
   .sidebar logo {
       display: block;
       color: lightgray;
       font-size: 1.4rem;
       text-align: center;
       background: #1b1b1b;
       user-select: none;
       line-height: 3rem;
   }
   .sidebar a {
       display: block;
       color: lightgray;
       height: 2.4rem;
       line-height: 2.4rem;
       padding-left: 1.6rem;
       width: 90%;
   }
   .sidebar a:hover, a.active {
       background: #2e486d;
       width: 240px;
   }
   .sidebar a i {
       font-size: 1.2rem;
       margin-right: 1.6rem;
   }
   .sidebar a span{
       letter-spacing: 1px;
       text-transform: uppercase;
   }
   #check-sidebar {
       display: none;
   }
   navnui {
       z-index: 5;
       margin-top: -20px;
       position: fixed;
       width: 100%;
       height: 42px;
       background-color: #23232e;
       color: lightgrey;
   }
   .navnui-left {
       float:left;
       margin-left: 80px;
   }
   .navnui-right {
       float:right;
   }

   header::after {
       content: '';
       display: table;
       clear: both;
   }
   navnui ul {
       margin: 0;
       padding: 0;
       list-style: none;
   }
   navnui li {
       float: left;
       display: inline-block;
       margin: 10px 10px 30px;
   }

   navnui a {
       color: #b6b6b6;
   }

   label #btn-sidebar, label #cancel-sidebar {
       z-index: 6;
       position: absolute;
       cursor: pointer;
       height: 37px;
       width: 37px;
       text-align: center;
       color: white;
       font-size: 24px;
       margin: -12px 30px 10px 30px;
       transition: all .5s ease;
   }

   label #cancel-sidebar {
       opacity:0;
       visibility: hidden;
   }
   #check-sidebar:checked ~ label #btn-sidebar {
       margin-left: 245px;
       opacity: 0;
       visibility: hidden;
   }
   #check-sidebar:checked ~ label #cancel-sidebar {
       margin-left: 245px;
       opacity: 1;
       visibility: visible;
   }
   #check-sidebar:checked ~ .sidebar{
       left:0;
   }

   #check-sidebar:checked ~ #navigation-left {
       margin-left: 310px;
   }
   @media (max-width:860px) {
       .sidebar{
           height: auto;
           width: 55px;
           left: 0;
           margin: 100px 0;
       }
       .sidebar logo, #btn-sidebar, #cancel-sidebar {
           display: none;
       }
       span {
           position: absolute;
           margin-left: 30px;
           opacity: 0;
           visibility: hidden;
       }
       .sidebar a i {
           margin-left: -15px;
       }
       a:hover{
           width: 240px;
           background: inherit;
       }
       .sidebar a:hover span {
           opacity: 1;
           visibility: visible;
       }
       .sidebar a.active {
           background: #2e486d;
           width: 240px !important;
       }
   }

</style>
