/**
 * 前台员工模块 Tailwind CSS 配置
 * 在页面head中引入此文件来配置Tailwind
 */
tailwind.config = {
    theme: {
        extend: {
            colors: {
                primary: '#165DFF',
                secondary: '#6B7280',
                success: '#00B42A',
                warning: '#FF7D00',
                danger: '#F53F3F',
                light: '#F9FAFB',
                dark: '#1F2937',
                neutral: '#F5F7FA',
                card: '#FFFFFF'
            },
            fontFamily: {
                inter: ['Inter', 'system-ui', 'sans-serif']
            },
            boxShadow: {
                'card': '0 2px 12px 0 rgba(0, 0, 0, 0.05)',
                'card-hover': '0 8px 24px 0 rgba(0, 0, 0, 0.1)'
            }
        }
    }
};
