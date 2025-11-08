class BannerAdapter(
    private val onBannerClick: (Banner) -> Unit,
    private val onNavItemClick: (NavItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_BANNER = 0
        private const val TYPE_NAV = 1
        private const val TYPE_WEEK = 2
    }

    private val bannerList = mutableListOf<Banner>()
    private val navItems = listOf(
        NavItem(R.drawable.ic_send, R.string.banner_latest, NavigationTarget.CATEGORIES),
        NavItem(R.drawable.ic_whatshot, R.string.banner_hot_ranking, NavigationTarget.CATEGORIES_HOT),
        NavItem(R.drawable.ic_aod, R.string.banner_hanman, NavigationTarget.CATEGORIES_HANMAN),
        NavItem(R.drawable.ic_import_contacts, R.string.banner_single_book, NavigationTarget.CATEGORIES_SINGLE),
        NavItem(R.drawable.ic_videogame, R.string.banner_games, NavigationTarget.GAMES),
        NavItem(R.drawable.ic_videocam, R.string.banner_movies, NavigationTarget.MOVIES),
        NavItem(R.drawable.ic_inventory, R.string.banner_library, NavigationTarget.LIBRARY)
    )

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_BANNER
            1 -> TYPE_NAV
            else -> TYPE_WEEK
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_BANNER -> BannerViewHolder(
                LayoutBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            TYPE_NAV -> NavViewHolder(
                LayoutNavGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            else -> WeekViewHolder(
                LayoutWeekPromoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BannerViewHolder -> holder.bind(bannerList)
            is NavViewHolder -> holder.bind(navItems)
            is WeekViewHolder -> holder.bind()
        }
    }

    override fun getItemCount(): Int = 3 // banner + nav + week

    fun submitList(banners: List<Banner>) {
        bannerList.clear()
        bannerList.addAll(banners)
        notifyItemChanged(0)
    }

    inner class BannerViewHolder(private val binding: LayoutBannerBinding) : 
        RecyclerView.ViewHolder(binding.root) {

        private val bannerAdapter = BannerPagerAdapter(onBannerClick)

        init {
            binding.bannerViewPager.apply {
                adapter = bannerAdapter
                offscreenPageLimit = 1
                setPageTransformer(ScaleInTransformer())
                
                // 自动轮播
                startAutoScroll(5000L)
            }
            
            binding.bannerIndicator.setViewPager(binding.bannerViewPager)
        }

        fun bind(banners: List<Banner>) {
            bannerAdapter.submitList(banners)
        }
    }

    inner class NavViewHolder(private val binding: LayoutNavGridBinding) : 
        RecyclerView.ViewHolder(binding.root) {

        private val navAdapter = NavGridAdapter(onNavItemClick)

        init {
            binding.navRecyclerView.apply {
                adapter = navAdapter
                layoutManager = GridLayoutManager(context, 4)
                addItemDecoration(GridSpacingItemDecoration(4, 16, true))
            }
        }

        fun bind(items: List<NavItem>) {
            navAdapter.submitList(items)
        }
    }

    inner class WeekViewHolder(private val binding: LayoutWeekPromoBinding) : 
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.weekImage.setOnClickListener {
                // 导航到周更页面
                onNavItemClick(NavItem(0, 0, NavigationTarget.WEEK))
            }
            
            // 加载周更GIF
            Glide.with(binding.root)
                .load(R.drawable.week_promo_gif)
                .into(binding.weekImage)
        }
    }
}

class BannerPagerAdapter(
    private val onBannerClick: (Banner) -> Unit
) : RecyclerView.Adapter<BannerPagerAdapter.BannerPageViewHolder>() {

    private val banners = mutableListOf<Banner>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerPageViewHolder {
        val binding = ItemBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BannerPageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BannerPageViewHolder, position: Int) {
        holder.bind(banners[position])
    }

    override fun getItemCount(): Int = banners.size

    fun submitList(newBanners: List<Banner>) {
        banners.clear()
        banners.addAll(newBanners)
        notifyDataSetChanged()
    }

    inner class BannerPageViewHolder(private val binding: ItemBannerBinding) : 
        RecyclerView.ViewHolder(binding.root) {

        fun bind(banner: Banner) {
            binding.root.setOnClickListener { onBannerClick(banner) }
            
            Glide.with(binding.root)
                .load(banner.imageUrl)
                .placeholder(R.drawable.banner_placeholder)
                .error(R.drawable.banner_error)
                .into(binding.bannerImage)
        }
    }
}

class NavGridAdapter(
    private val onItemClick: (NavItem) -> Unit
) : ListAdapter<NavItem, NavGridAdapter.NavItemViewHolder>(NavItemDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavItemViewHolder {
        val binding = ItemNavGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NavItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NavItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NavItemViewHolder(private val binding: ItemNavGridBinding) : 
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NavItem) {
            binding.navIcon.setImageResource(item.iconRes)
            binding.navLabel.setText(item.labelRes)
            
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    object NavItemDiffCallback : DiffUtil.ItemCallback<NavItem>() {
        override fun areItemsTheSame(oldItem: NavItem, newItem: NavItem): Boolean {
            return oldItem.labelRes == newItem.labelRes
        }

        override fun areContentsTheSame(oldItem: NavItem, newItem: NavItem): Boolean {
            return oldItem == newItem
        }
    }
}