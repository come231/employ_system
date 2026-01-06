/**
 * 侧边栏菜单专属JS - 展开/收起/激活事件
 */
function bindSidebarEvents() {
    console.log("侧边栏事件绑定成功")
    // 菜单列表
    const menus = [
        { main: 'menuUser', sub: 'submenuUser' },
        { main: 'menuAttendance', sub: 'submenuAttendance' },
        { main: 'menuAnalysis', sub: 'submenuAnalysis' },
        { main: 'menuSetting', sub: 'submenuSetting' }
    ];

    // 绑定每个菜单的点击事件
    menus.forEach(item => {
        const mainMenu = document.getElementById(item.main);
        const subMenu = document.getElementById(item.sub);

        mainMenu.addEventListener('click', function(e) {
            e.preventDefault(); // 主菜单仍阻止跳转（因为href是#）
            // 切换子菜单显示/隐藏
            subMenu.classList.toggle('show');
            // 切换箭头方向
            const arrow = this.querySelector('.fa-angle-down') || this.querySelector('.fa-angle-up');
            if (subMenu.classList.contains('show')) {
                arrow.classList.remove('fa-angle-down');
                arrow.classList.add('fa-angle-up');
            } else {
                arrow.classList.remove('fa-angle-up');
                arrow.classList.add('fa-angle-down');
            }
        });
    });

    // 首页菜单激活状态
    document.getElementById('menuHome').addEventListener('click', function(e) {
        // 移除阻止跳转 - 首页需要正常跳转
        // 移除所有激活状态
        document.querySelectorAll('.sidebar-menu-link').forEach(link => {
            link.classList.remove('active-parent');
        });
        document.querySelectorAll('.sidebar-submenu-link').forEach(link => {
            link.classList.remove('active');
        });
        // 添加首页激活状态
        this.classList.add('active-parent');

        // 新增：点击首页时，收起所有子菜单
        menus.forEach(item => {
            const subMenu = document.getElementById(item.sub);
            const arrow = document.getElementById(item.main).querySelector('.fa-angle-up');
            if (subMenu.classList.contains('show')) {
                subMenu.classList.remove('show');
            }
            if (arrow) {
                arrow.classList.remove('fa-angle-up');
                arrow.classList.add('fa-angle-down');
            }
        });
    });

    // 为所有子菜单链接添加点击事件
    document.querySelectorAll('.sidebar-submenu-link').forEach(link => {
        link.addEventListener('click', function(e) {
            // 移除e.preventDefault() - 恢复子菜单跳转功能
            // 移除所有激活状态
            document.querySelectorAll('.sidebar-menu-link').forEach(link => {
                link.classList.remove('active-parent');
            });
            document.querySelectorAll('.sidebar-submenu-link').forEach(link => {
                link.classList.remove('active');
            });
            // 激活当前子菜单
            this.classList.add('active');
            // 自动展开对应的父菜单
            const parentSubmenu = this.closest('.sidebar-submenu');
            parentSubmenu.classList.add('show');
            // 切换父菜单箭头
            const parentMenu = parentSubmenu.previousElementSibling;
            const arrow = parentMenu.querySelector('.fa-angle-down') || parentMenu.querySelector('.fa-angle-up');
            arrow.classList.remove('fa-angle-down');
            arrow.classList.add('fa-angle-up');
        });
    });
}

// 初始化侧边栏（供主页面调用）
function initSidebar() {
    bindSidebarEvents();

    // 新增：页面加载完成后，检查是否有激活的子菜单，确保父菜单展开且箭头正确
    const activeSubLink = document.querySelector('.sidebar-submenu-link.active');
    if (activeSubLink) {
        const parentSubmenu = activeSubLink.closest('.sidebar-submenu');
        const parentMenu = parentSubmenu.previousElementSibling;
        const arrow = parentMenu.querySelector('.fa-angle-down') || parentMenu.querySelector('.fa-angle-up');

        // 确保子菜单展开
        parentSubmenu.classList.add('show');
        // 确保箭头方向正确
        if (arrow) {
            arrow.classList.remove('fa-angle-down');
            arrow.classList.add('fa-angle-up');
        }
    }
}
