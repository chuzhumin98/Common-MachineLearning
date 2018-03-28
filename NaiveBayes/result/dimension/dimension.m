clear all
[data] = textread('dimension.txt');
index = [' Accuracy ';' Precision';'  Recall  ';'F1-Measure'];
X = 1:6;
average = [];
max = [];
min = [];
%% 由于三部分的信息（均值、最大值、最小值）混合在一起，这里需要先剥离开
for (i = 1:18)
    if (mod(i,3)==1)
        average = [average; data(i,:)];
    end
    if (mod(i,3)==2)
        max = [max; data(i,:)];
    end
    if (mod(i,3)==0)
        min = [min; data(i,:)];
    end
end
%% 分别在四张图中绘制出它们并保存
for i=1:4
    plot(X, average(:,i),'.-','MarkerSize',20,'LineWidth',1.5)
    hold on
    plot(X, max(:,i),'.-','MarkerSize',20,'LineWidth',1.5)
    plot(X, min(:,i),'.-','MarkerSize',20,'LineWidth',1.5)
    xlim([0.5 6.5])
    set(gca, 'xTick', 1:6);  
    set(gca,'XTickLabel',{'100','200','500','1000','2000','5000'})  
    legend('Average','Max','Min','Location','SouthEast')
    xlabel('特征总维数')
    ylabel(index(i,:))
    title(strcat(index(i,:),'随特征维数的变化关系'))
    box on
    grid on
    saveas(gcf,strcat(index(i,:),'随特征维数的变化关系.png'))
    if (i ~= 4)
        figure
    end
end