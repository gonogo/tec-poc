// Override gem InPageNavigation so clean URLs (/path/) match
// relative sidebar links (.../path/index.html) for toc-link--in-view.
// Loaded after govuk_tech_docs; document.ready has not run yet.
(function ($, Modules) {
  'use strict'

  Modules.InPageNavigation = function InPageNavigation () {
    var $contentPane
    var $tocItems
    var $targets

    this.start = function start ($element) {
      $contentPane = $element.find('.app-pane__content')
      $tocItems = $('.js-toc-list').find('a')
      $targets = $contentPane.find('[id]')

      $(window).on('scroll', _.debounce(handleScrollEvent, 100, { maxWait: 100 }))

      if (Modernizr.history) {
        $(window).on('popstate', function (event) {
          restoreScrollPosition(event.originalEvent.state)
        })

        if (history.state && history.state.scrollTop) {
          restoreScrollPosition(history.state)
        } else {
          window.requestAnimationFrame(handleInitialLoadEvent)
        }
      }
    }

    function restoreScrollPosition (state) {
      if (state && typeof state.scrollTop !== 'undefined') {
        window.scrollTo(0, state.scrollTop)
      }
    }

    function handleInitialLoadEvent () {
      var fragment = fragmentForTargetElement()

      if (!fragment) {
        fragment = fragmentForFirstElementInView()
      }

      highlightActiveItemInToc(fragment)
    }

    function handleScrollEvent () {
      var fragment = fragmentForFirstElementInView()

      storeCurrentPositionInHistoryApi(fragment)
      highlightActiveItemInToc(fragment)
    }

    function storeCurrentPositionInHistoryApi (fragment) {
      if (Modernizr.history && fragment) {
        history.replaceState(
          { scrollTop: window.scrollY || window.pageYOffset },
          '',
          fragment
        )
      }
    }

    // Treat /foo/, /foo and /foo/index.html as the same location.
    function normalizePath (pathname) {
      return pathname
        .replace(/\/index\.html$/i, '')
        .replace(/\/$/, '') || '/'
    }

    function pathsMatch (left, right) {
      return normalizePath(left) === normalizePath(right)
    }

    function highlightActiveItemInToc (fragment) {
      var $activeTocItem = $tocItems.filter(function (_) {
        var url = new URL($(this).attr('href'), window.location.href)
        return pathsMatch(url.pathname, window.location.pathname) &&
          url.hash === window.location.hash
      })

      // Page links (no hash) when no fragment item matched.
      if (!$activeTocItem.get(0)) {
        $activeTocItem = $tocItems.filter(function (_) {
          var url = new URL($(this).attr('href'), window.location.href)
          return url.hash === '' && pathsMatch(url.pathname, window.location.pathname)
        })
      }
      if ($activeTocItem.get(0)) {
        $tocItems.removeClass('toc-link--in-view')
        $activeTocItem.addClass('toc-link--in-view')
        scrollTocToActiveItem($activeTocItem.get(0))
      }
    }

    function scrollTocToActiveItem (activeTocElement) {
      activeTocElement.scrollIntoView({ block: 'nearest', inline: 'nearest' })
    }

    function fragmentForTargetElement () {
      return window.location.hash
    }

    function fragmentForFirstElementInView () {
      var result = null

      $($targets.get().reverse()).each(function checkIfInView (index) {
        if (result) {
          return
        }

        var $this = $(this)

        if (Math.floor($this[0].getBoundingClientRect().top) <= 0) {
          result = $this
        }
      })

      return result ? '#' + result.attr('id') : false
    }
  }
})(jQuery, window.GOVUK.Modules)
