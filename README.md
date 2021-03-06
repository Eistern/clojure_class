# Объектная модель для языка Clojure
## Базовые требования:
1. Определите для языка Clojure объектную модель. Обеспечите поддержку следующих
   элементов:
   * класс;
   * атрибут (свойство, слот);
   * множественное наследование (в предположении что все ветки не
   пересекаются по атрибутам и методам);
   * диспетчеризацию обработки сообщений относительно одного аргумента
   (динамический полиморфизм).
2. Определите соответствующие функции и/или макросы для работы с перечисленными
   выше элементами.

## Функционал
### Создание классов
Макрос `declare_class` позволяет определить класс, его суперклассы и его атрибуты. 
Сигнатура вызова:
```clojure
(declare_class! class_name [vector_of_attributes] [vector_of_superclasses])
```
`vector_of_superclasses` - спискок суперклассов. Допускается множественное наследование. Все классы связаны одной иерархией наследования, так как все классы явно или неявно наследуют самый общий тип T (аналог Object в java). Если `(list_of_superclasses)` отсутствует, то предполагается, что тип является потомком общего T.

`vector_of_attributes` - список атрибутов класса, атрибут задается списком вида:
```clojure
(:attr_name (list_init_macros))
```
`list_init_macros` - список макросов, позволяющий инициализовать переменные, назначать значения по умолчанию и т.д. В дальнейшем можно расширять список макросов для атрибутов для добавления новой функцинальности.

Минимальный список макросов для реализации:
* `default_value` - используется для задания начального значения атрибута.

**Пример объявления классов:**
```clojure
(declare_class! "message" [
  [:message (default_value "it's me")]
  [:message_count (default_value 0)]
  [:additional_message]
  ]
)

(declare_class "hello_message" [
  [:hello_message (default_value "hello")]
  ] ["message"]
)

(declare_class "goodbye_message" [
  [:goodbye_message (default_value "bye")]
  ] ["message"]
)
```

### Создание объекта
Метод 'new_obj' позволяет создать объект заданного класса. Сигнатура метода приведена ниже:
```clojure
(new_obj class_name)
```

### Работа с атрибутами
Для работы с атрибутами необходимы методы чтения и записи значения в атрибут.
Если не установлено значение атрибута по умолчанию, метод чтения вернет специальное значение `nil`, означающее, что значение не задано.

Сигнатура метода чтения:
```clojure
(get_value class_object :attribute_name)
```
Метод записи значения возвращает объект, в который была проведена запись. Сигнатура метода записи значения в атрибут:
```clojure
(set_value! class_object :attribute_name new_value_name)
```

Подробнее о праметрах методов:

`class_object` - объект, атрибуты которого извлекаются или задаются.
`attribute_name` - имя атрибута, значение которого извлекается или задается.
`new_value_name`- новое устанавливаемое значение атрибута.

**Пример работы с атрибутами**
```clojure
(let base_object (new_obj message) 
  (get_value base_object :message) ; returns "it's me" 
  (get_value base_object :additional_message)  ; returns nil
  
  (set_value! base_object :message "new message!") ; returns "new message!" 
  (set_value! base_object :additional_message "it's not empty now")  ; returns "it's not empty now"
  
  (get_value base_object :message) ; returns "new message!" 
  (get_value base_object :additional_message)  ; returns "it's not empty now"
                   
)
```

### О реализации классов
1. Все классы связаны одной иерархией наследования. Корень дерева наследовавния - общий класс `T`
2. Допускается множественное наследование
3. Имена классов, соответсвующие список пар атрибут-значение по умолчанию, список суперклассов хранятся в памяти
4. Подумать, как потенциально можно ускорить поиск класса по имени?. Поиск по имени выполняется с помощью map, где ключ - имя класса
5. Объекты хранятся в памяти. Объект представляет собой значение его атрибутов (определенных в самом классе и наследованных от суперклассов) и имя класса
6. Атрибуты наследуются и доступны в потомках

### Динамический полиморфизм
Предлагается реализация `generic functions` (подробнее о [generic-functions](https://gigamonkeys.com/book/object-reorientation-generic-functions.html)). Сигнатура определения такого метода:
```clojure
(declare_generic! "method_name" [parameters_vector])
```
`method_name` - имя объявляемого метода
`parameters_vector` - список имен параметров метода

Для объявления конкретного метода для параметров с определенным типом необходимо использовать ``:
```clojure
(declare_method! "method_name" [additional_flags] [list_parameters_with_types] (fn[] function))
```
`list_parameters_with_types` - вектор вектор-пар из имени параметра и его типа. Имя метода и его аргуметов должны совпадать с объявленными в `declare_method`.
`additional_flags` - вектор с дополнительными указаниями для метода. Например, с их помощью можно реализовать `before` и `after` методы, указывать флаг `call_next`

При вызове метода на основе типов его параметров составляется эффективный метод из всех подходящих generic функций.
Алгоритм построения эффективного метода:
1. Среди всех объявленных generic методов найти подходящие по имени. Затем среди них найти подходящие по именам параметров. Для простоты можно допускать, что параметры нельзя переупорядочивать.
2. Затем отобрать те generic методы, тип параметров которых подходит к типу переданного значения в непосредственном вызове метода. Типы подходят, если они совпадают или если тип аргумента в generic методе является супертипом переданного в вызове типа.
3. Упорядочить отобранные generic методы в порядке близости по типам к аргументам непосредственно вызова. Наиболее близким являются совпадающие типы.
4. Вызов generic методов от наиболее близких к наиболее дальним в отсортированном порядке.

Предлагаемый алгоритм сортировки:
1. Вычислить для каждого generic метода некоторое значение `g_metric` относительно типов аргументов непсредственного вызова метода.
2. Чем меньше `g_metric`, тем более близок generic метод к типам вызова.
3. `g_metric` вычисляется суммированием значений дальности `dist` для типов всех аргументов в вызове и generic методе.
   `dist` равен расстоянию между типами по иерархии наследования (Для совпадающих типов `dist` равен 0). 

Например, Есть классы: A, B (наследник A), C (наследник B). Тогда `dist(A, A) = 0`. `dist(A, B) = 1`. `dist(A, C) = 2`.
Если есть метод `(declare_method method((a A) (b B)))`, а также непосредственный вызов `method((new A) (new C))`, то `g_metric` типов таких аргументов равен `1`.

Для множественного наследования:
Стоит упорядочивать по следующему правилу: если тип имеет несколько суперклассов, то среди них наиболее близким является указанный первым в списке суперклассов при объявлении `declare_class`.

### Контроль цепочки вызовов generic методов
Как уже упоминалось, generic методы вызываются в отсортированном по типам аргументов порядке.
По умолчанию метод, объявленный `declare_method` не осуществляет такого перехода к следующему методу по порядку. Явное указание флага `call-next` для объявления метода позволяет перейти к выполнению следующего метода в отсортированном порядке.

**Приведем пример для пояснения того, как должен работать динамический полиморфизм:**
```clojure
(declare_generic! "say_something" ["msg" "additional_info"])

; 1 - Не будет вызван, так как в предыдущем методе не указан call-next-method
(declare_method! "say_something" [] [["msg" "T"] ["additional_info" "T"]]
                 (fn[] (print_error "don't call me"))
                )
; 2 - Будет вызван, так как в предыдущем подходящем указан call-next-method
(declare_method! "say_something" [] [["msg" "message"] ["additional_info" "T"]]
                 (fn[] (println "Third specific"))
                )
; 3- Будет вызван, так как в предыдущем подходящем указан call-next-method
(declare_method! "say_something" [:call_next] [["msg" "hello_message"] ["additional_info" "T"]]
                (println "Second specific")
                )

; Самый подходящий по типам к вызову, будет вызван первым
(declare_method! "say_something" [:call_next] [["msg" "hello_message"] ["additional_info" "message"]]
                 (fn[params] (println "The most specific") (println (get_param_by_name "msg" params)))
                )
; Непосредственно вызов с параметрами типа hello_message и message
(call "say_something" [(new_obj "hello_message") (new_obj "message")])
```

Ожидаемый вывод:
``
"The most specific"
"Second specific"
"Third specific"
``

## Этапы реализации
Основной функционал:

|Описание|Дата завершения|Ответственный за реализацию|
|---|---|---|
|Реализация классов, их сохранения в памяти| 10.11 | Даниил Зулин |
|Реализация создания объектов, функций чтения/записи атрибутов| 15.11 | Даниил Зулин |
|Добавление наследования к классам, в том числе множественного| 20.11 | Даниил Зулин |
|Реализация базовых возможностей динамического полиморфизма с наивной реализацией сортировки для одного параметра| 30.11 | Дарья Усова |

Дополнительный функционал:

|Описание|Дата завершения|Ответственный за реализацию|
|---|---|---|
|Разработка и реализация алгоритма сортировки для множественного наследования и нескольких параметров в методе| 15.12 | Дарья Усова |
|Реализация call-next-method| 21.12 | Дарья Усова |
