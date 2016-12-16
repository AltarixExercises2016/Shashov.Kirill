# Транспорт Самары
Автор: Шашов Кирилл

Приложение является клиентом для сервиса http://tosamara.ru/. 
Информация о предоставленном API: http://tosamara.ru/api/

##Описание
###Минимальный функционал
Приложение должно иметь 4 экрана:
* Экран с загрузкой обновлений вспомогательных справочный файлов (например, с остановками)
* Список остановок (ближайшие к пользователю)
* Настройки (например, дистанция поиска остановок)
* Список прибывающего транспорта для конкретной остановки. 

Требования:
* По возможности соответствовать современным требованиям по дизайну.
* Корректно обрабатывать смену конфигурации (например, ориентация экрана)
* Использовать базу данных с ORM фреймворком

###Пожелания:
* Просмотр маршрута по номеру
* Полнотекстовый поиск остановок по названию

##Новости
###27.11.2016
Загружено 2 проекта:
* [dao-gen](dao-gen/) Проект для генерации классов для функционирования GreenDao
* [app](app/) Основное приложение

Добавлены 2 активити:
* `com.transportsmr.app.SplashScreenActivity` Отвечает за экран загрузки. При доступе в интернет проверяется наличие обновлений файлов (пока только справочник остановок). Если файл устарел, он скачивается, парсится, информация заносится в базу данных. Работа с сетью и обновление БД выполняются в `com.transportsmr.app.async.ClassifiersUpdateTask`. В качестве прослойки между таской и активити используется фрагмент с установленным свойством `setRetainInstance(true)` для независимости от жизненного цикла активити. Взаимодействие между активити, фрагметом, таской осуществляется посредством листенеров для минимизации связи между компонентами.
* `com.transportsmr.app.MainActivity` В качестве тулбара используется `android.support.v7.widget.Toolbar`, стандартный тулбар скрыт стилями. Активити использует Navigation Drawer для отображения меню. 

###16.12.2016
Добавлены фрагменты:
* `com.transportsmr.app.fragments.SettingsFragment` Для изменения радиуса поиска остановок. В качестве слайдера используется библиотека `org.adw.library:discrete-seekbar`
* `com.transportsmr.app.fragments.StopsFragment` Для отображения ближайших/сохраненных остановок. Используется `TabLayout + ViewPager`. Для отображения кнопки избранного используется библиотека `com.github.ivbaranov:materialfavoritebutton`
* `com.transportsmr.app.fragments.ArrivalsFragment` Для просмотра прибытия транспорта для конкретной остановки. Для загрузки информации используется AsyncTask.


Скриншоты:
<img src="https://github.com/AltarixExercises2016/Shashov.Kirill/blob/master/img/1.png" width="250">
<img src="https://github.com/AltarixExercises2016/Shashov.Kirill/blob/master/img/2.png" width="250">
<img src="https://github.com/AltarixExercises2016/Shashov.Kirill/blob/master/img/3.png" width="250">
<img src="https://github.com/AltarixExercises2016/Shashov.Kirill/blob/master/img/4.png" width="250">
<img src="https://github.com/AltarixExercises2016/Shashov.Kirill/blob/master/img/5.png" width="250">


